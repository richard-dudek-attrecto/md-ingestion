package com.rddk.mdingestion.pipeline;

import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;

/**
 * Downstream boundary of the ingestion pipeline: the point where normalized,
 * lateness-classified events leave the pipeline and are handed off for downstream
 * processing. A sink only <em>accepts</em> data; it is not a queryable store.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
public interface TelemetrySink {

    /**
     * Publishes a normalized event to the downstream boundary.
     *
     * @param event the event to publish
     */
    void publish(NormalizedTelemetryEvent event);
}
