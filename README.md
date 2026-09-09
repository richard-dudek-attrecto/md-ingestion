# Multi-Device Data Ingestion Prototype

A small Spring Boot (Java 21) service that ingests telemetry from **different kinds of devices**,
normalizes each device-specific payload into a **single common model**, handles **late / out-of-order**
data explicitly, and makes the normalized stream available for **downstream processing**.

Two device categories are supported out of the box:

| Category | Example device | `DeviceType` | Payload shape |
|---|---|---|---|
| Simple numeric | Temperature sensor | `NUMERIC` | one reading per message |
| Bursty | Vibration sensor | `BURSTY` | an array of samples per message |

## Architecture

```
                 ┌──────────────────────────────────────────────────────────────┐
                 │                       IngestionService                         │
 HTTP POST       │                                                                │
 raw DTO  ──────▶│  1. StrategyRegistry.normalize(deviceType, dto)                │
                 │        └─ DeviceIngestionStrategy<T>  (per device type)        │
                 │  2. LateArrivalPolicy.evaluate(event)  (per-device watermark)  │
                 │  3. TelemetrySink.publish(event)                               │
                 └───────────────────────────────┬──────────────────────────────┘
                                                  │ publish
                                                  ▼
                                      InMemoryTelemetrySink ──▶ InMemoryTelemetryStore
                                      (pure sink)               (main + late buffers)
                                                                        │ read
                                                                        ▼
                                             TelemetryInspectionService  (TESTING ONLY)
                                                                        │
                          GET /api/v1/events ◀──────────────────────────┘  browser test console
```

See the published architecture document at https://richard-dudek-attrecto.github.io/md-ingestion/architecture.html for the full architecture overview, including an SVG diagram, the lateness model, and developer next-steps.

> ⚠️ **The inspection layer is for testing/visualization only.** The `inspection` package
> (`TelemetryInspectionService` + `TelemetryInspectionController`, endpoints `GET /api/v1/events`
> and `/api/v1/events/late`) exists purely so the in-memory buffer can be viewed from the browser
> test console. It is **not** part of the production downstream contract — a real consumer reads
> events from the `TelemetrySink` downstream (DB / Kafka / MQ), not back over HTTP.

### Key components

- **`NormalizedTelemetryEvent`** (`domain`) — the common internal model every payload is
  mapped to (`eventId`, `deviceId`, `deviceType`, `timestamp`, `ingestedAt`, `metrics`, `isLate`).
- **`DeviceIngestionStrategy<T>`** (`strategy`) — Strategy interface. One implementation per
  device type (`TemperatureStrategy`, `VibrationStrategy`). Strategies only do **format
  translation** — they contain no lateness logic.
- **`IngestionStrategyRegistry`** — collects all strategy beans into an `EnumMap<DeviceType, …>`
  and resolves the correct one at runtime.
- **`LateArrivalPolicy` / `WatermarkLateArrivalPolicy`** (`pipeline`) — centralized, per-device
  watermark logic that classifies each event as `ON_TIME`, `OUT_OF_ORDER`, or `LATE`.
- **`TelemetrySink` / `InMemoryTelemetrySink`** (`pipeline`) — downstream boundary. A **pure sink**:
  it only `publish`es events (it is not queryable). The in-memory implementation forwards them to
  `InMemoryTelemetryStore` (main buffer + late buffer).
- **`IngestionService`** — orchestrates normalize → classify → publish.
- **`IngestionController`** (`controller`) — write side; accepts raw device payloads.
- **`TelemetryInspectionService` / `TelemetryInspectionController`** (`inspection`) —
  **testing/visualization only** (see note above). Read-only view over the in-memory buffer.

## API

```
POST /api/v1/ingest/temperature   {"deviceId":"t-1","temperatureValue":21.5,"recordedAt":"2026-01-01T00:00:00Z"}
POST /api/v1/ingest/vibration     {"deviceId":"v-1","samples":[0.1,0.2,0.3],"recordedAt":"2026-01-01T00:00:00Z"}
```

## VISUALISATION API - just for visualization

```
GET  /api/v1/events[?deviceId=v-1] # TESTING/VISUALISATION ONLY — inspect buffered normalized events
GET  /api/v1/events/late           # TESTING/VISUALISATION ONLY — events flagged as LATE
```

## How late / out-of-order data is handled

Each device has its own **watermark** = the highest event timestamp seen so far for that device.
Every incoming event is compared against it:

- `timestamp >= watermark` → **ON_TIME** (watermark advances)
- `timestamp < watermark` but within `ingestion.allowed-lateness` → **OUT_OF_ORDER**
  (still delivered downstream; consumers may re-sort using `timestamp`)
- older than the allowed-lateness window → **LATE** (`isLate = true`, also routed to the late buffer)

The classification is stamped onto the event (`isLate` flag + `metrics.lateness`) so it travels
with the data instead of being silently dropped. The tolerance is configurable
(`ingestion.allowed-lateness`, default `PT30S`). This is deliberately **explicit**: nothing is
discarded, and downstream code can decide what to do with out-of-order and late events.

## How the large / bursty payload is handled

A vibration message carries an **array of samples**. `VibrationStrategy` fans the burst out into
one `NormalizedTelemetryEvent` per sample, deriving a per-sample timestamp
(`recordedAt + index * 100ms`) and attaching `sampleIndex` / `sampleValue` / `sampleCount` to
`metrics`. This gives every sample the same shape as a simple numeric reading, so the rest of the
pipeline (lateness, sink, downstream) treats bursty and numeric devices identically.

## Key assumptions

- Device inputs are simulated over HTTP JSON; no real hardware or broker integration.
- In-memory storage is sufficient (per the brief); nothing is persisted across restarts.
- Vibration sample spacing is assumed at 100ms; a real payload would carry per-sample timing.
- A single-node deployment, so the watermark map lives in process memory.

## Tradeoffs

- **Synchronous in-process pipeline** instead of a message broker — simplest thing that satisfies
  "make data available downstream" within the timebox, while still isolating the sink behind an
  interface so it can be swapped for Kafka/queue later.
- **Per-device watermark in a `ConcurrentHashMap`** — simple and explicit; not distributed and the
  map grows with device count (fine for a prototype).
- **Fan-out per sample** increases event count for bursty devices but keeps a uniform downstream
  model; an alternative (one event holding the whole array) was rejected to keep downstream logic
  uniform.
- **Unbounded in-memory buffers** — acceptable for a prototype; production would need bounding/eviction.

## Extensibility

Adding a new device type requires **no changes to existing code**:
1. add a value to `DeviceType`,
2. add a DTO,
3. implement `DeviceIngestionStrategy<NewDto>` as a `@Component`.

Spring injects it into the registry automatically (Open-Closed Principle).

## Technology stack

- Java: 21
- Maven: 3.9.x
- Spring Boot: 3.3.3
- Spring Web / validation starter
- JUnit 5 + AssertJ for tests

## Build & run

```bash
mvn test          # run the focused test suite
mvn spring-boot:run
```

Once running, open **http://localhost:8080/** for the browser test console (send payloads and
visualise the normalized/late results). The console uses the testing-only inspection endpoints.

## Next steps (production path)

The `TelemetrySink` interface is the seam for going to production — swap the implementation without
touching the controller, strategies, or policy:

- **Persist to a database:** a `JpaTelemetrySink` writing each `NormalizedTelemetryEvent` to
  Postgres/MySQL for querying and audit.
- **Publish to Kafka:** a `KafkaTelemetrySink` (via `KafkaTemplate`) to a topic keyed by `deviceId`,
  for high-throughput streaming and multiple consumers.
- **Publish to RabbitMQ / JMS:** a `RabbitTelemetrySink` for classic queue fan-out / work queues.
- Route **LATE** events to a dead-letter topic/queue/table for separate reprocessing.
- **Retire the inspection layer** for production (delete it or guard it behind a `@Profile("dev")`).
- Additional refinements: per-burst lateness evaluation, bounded buffers/back-pressure, idempotency
  via `eventId`, externalized watermarks (Redis/DB) for multi-instance deployments, and metrics.

See the published architecture document at https://richard-dudek-attrecto.github.io/md-ingestion/architecture.html for the detailed diagram and rationale.

## Tests

Focused tests cover the most important behavior:
- `WatermarkLateArrivalPolicyTest` — on-time / out-of-order / late classification + per-device isolation
- `VibrationStrategyTest` — bursty fan-out and empty-burst handling
- `IngestionStrategyRegistryTest` — strategy resolution and unknown-type failure
- `IngestionFlowIntegrationTest` — end-to-end ingest → downstream availability + validation
