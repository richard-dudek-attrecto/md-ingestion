package com.rddk.mdingestion.pipeline;

import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * In-memory buffer that retains published telemetry events for this prototype.
 *
 * <p>It is written to by {@link InMemoryTelemetrySink} and read from by the
 * testing-only inspection layer. Events are kept in a thread-safe queue, with late
 * events mirrored into a separate buffer so they can be inspected without scanning
 * the whole stream. No external infrastructure is required.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
@Component
public class InMemoryTelemetryStore {

    private final ConcurrentLinkedQueue<NormalizedTelemetryEvent> events = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<NormalizedTelemetryEvent> lateEvents = new ConcurrentLinkedQueue<>();

    /**
     * Records an event, additionally mirroring it into the late buffer when it is late.
     *
     * @param event the event to record
     */
    public void record(NormalizedTelemetryEvent event) {
        events.add(event);
        if (event.isLate()) {
            lateEvents.add(event);
        }
    }

    /**
     * Returns all recorded events.
     *
     * @return an immutable snapshot of every recorded event
     */
    public List<NormalizedTelemetryEvent> all() {
        return List.copyOf(events);
    }

    /**
     * Returns the recorded events for a single device.
     *
     * @param deviceId the device identifier to filter by
     * @return the matching events in recording order
     */
    public List<NormalizedTelemetryEvent> byDevice(String deviceId) {
        return events.stream()
                .filter(event -> event.deviceId().equals(deviceId))
                .toList();
    }

    /**
     * Returns only the events that were classified as late.
     *
     * @return an immutable snapshot of the late events
     */
    public List<NormalizedTelemetryEvent> late() {
        return List.copyOf(lateEvents);
    }
}
