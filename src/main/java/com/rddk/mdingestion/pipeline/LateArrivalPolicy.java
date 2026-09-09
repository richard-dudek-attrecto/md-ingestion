package com.rddk.mdingestion.pipeline;

import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;

/**
 * Classifies a normalized event as on-time, out-of-order, or late and returns a
 * copy of the event stamped with that classification. Implementations decide the
 * concrete strategy (e.g. per-device watermark, fixed window).
 *
 * @author richard.dudek
 * @since 0.0.1
 */
public interface LateArrivalPolicy {

    /**
     * Evaluates the timeliness of an event and stamps it with the resulting classification.
     *
     * @param event the normalized event to classify
     * @return a copy of the event carrying its lateness classification
     */
    NormalizedTelemetryEvent evaluate(NormalizedTelemetryEvent event);
}
