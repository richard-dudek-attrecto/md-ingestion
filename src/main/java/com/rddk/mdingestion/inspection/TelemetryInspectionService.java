package com.rddk.mdingestion.inspection;

import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;
import com.rddk.mdingestion.pipeline.InMemoryTelemetryStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Read-only helper that exposes the buffered telemetry for visual inspection.
 *
 * <p><strong>Testing / demo only.</strong> In a real deployment the normalized events
 * would be consumed from the {@link com.rddk.mdingestion.pipeline.TelemetrySink} by a
 * downstream system (database, message broker, stream processor). This service exists
 * purely so the state of the in-memory buffer can be visualised from the browser test
 * console; it is not part of the production ingestion pipeline.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
@Service
public class TelemetryInspectionService {

    private final InMemoryTelemetryStore store;

    /**
     * Creates the inspection service backed by the in-memory store.
     *
     * @param store the buffer to read from
     */
    public TelemetryInspectionService(InMemoryTelemetryStore store) {
        this.store = store;
    }

    /**
     * Returns all buffered events (testing/visualisation only).
     *
     * @return an immutable snapshot of every buffered event
     */
    public List<NormalizedTelemetryEvent> all() {
        return store.all();
    }

    /**
     * Returns the buffered events for a single device (testing/visualisation only).
     *
     * @param deviceId the device identifier to filter by
     * @return the matching events in recording order
     */
    public List<NormalizedTelemetryEvent> byDevice(String deviceId) {
        return store.byDevice(deviceId);
    }

    /**
     * Returns only the events classified as late (testing/visualisation only).
     *
     * @return an immutable snapshot of the late events
     */
    public List<NormalizedTelemetryEvent> late() {
        return store.late();
    }
}
