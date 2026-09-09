package com.rddk.mdingestion.pipeline;

import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;
import org.springframework.stereotype.Component;

/**
 * In-memory {@link TelemetrySink} for this prototype. It is a pure sink: it only
 * accepts events and hands them to the {@link InMemoryTelemetryStore}. Reading the
 * buffered data is intentionally not part of the sink; that lives in the testing-only
 * inspection layer.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
@Component
public class InMemoryTelemetrySink implements TelemetrySink {

    private final InMemoryTelemetryStore store;

    /**
     * Creates the sink backed by the in-memory store.
     *
     * @param store the buffer events are written to
     */
    public InMemoryTelemetrySink(InMemoryTelemetryStore store) {
        this.store = store;
    }

    /**
     * Hands the event off to the in-memory store.
     *
     * @param event the event to publish
     */
    @Override
    public void publish(NormalizedTelemetryEvent event) {
        store.record(event);
    }
}
