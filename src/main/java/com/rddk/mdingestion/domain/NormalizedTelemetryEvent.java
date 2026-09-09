package com.rddk.mdingestion.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unified, device-agnostic representation of a single telemetry reading. Every device-specific payload is normalized into one or more of these
 * events.
 *
 * @param eventId
 *         globally unique identifier of the event
 * @param deviceId
 *         identifier of the originating device
 * @param deviceType
 *         category of the originating device
 * @param timestamp
 *         time the reading occurred, according to the device
 * @param ingestedAt
 *         time the event was accepted by the service
 * @param metrics
 *         device-agnostic key/value measurements (defensively copied, never {@code null})
 * @param isLate
 *         whether the event was classified as {@link LatenessClassification#LATE}
 * @author richard.dudek
 * @since 0.0.1
 */
public record NormalizedTelemetryEvent(
        String eventId,
        String deviceId,
        DeviceType deviceType,
        Instant timestamp,
        Instant ingestedAt,
        Map<String, Object> metrics,
        boolean isLate
) {
    /**
     * Canonical constructor that defensively copies {@code metrics} so the event stays immutable, tolerating a {@code null} map by substituting an
     * empty one.
     */
    public NormalizedTelemetryEvent {
        metrics = metrics == null ? Map.of() : Map.copyOf(metrics);
    }

    /**
     * Returns a copy of this event stamped with the given lateness classification. The classification is also recorded in the metrics so it travels
     * with the event to downstream consumers.
     *
     * @param classification
     *         the lateness classification to stamp onto the copy
     * @return a new event carrying the classification (in {@code metrics.lateness} and {@code isLate})
     */
    public NormalizedTelemetryEvent withLateness(LatenessClassification classification) {
        Map<String, Object> enriched = new LinkedHashMap<>(metrics);
        enriched.put("lateness", classification.name());
        return new NormalizedTelemetryEvent(
                eventId,
                deviceId,
                deviceType,
                timestamp,
                ingestedAt,
                enriched,
                classification == LatenessClassification.LATE
        );
    }
}
