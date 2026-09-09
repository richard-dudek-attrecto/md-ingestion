package com.rddk.mdingestion.pipeline;

import com.rddk.mdingestion.domain.LatenessClassification;
import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Watermark-based late/out-of-order handling.
 *
 * <p>Each device carries its own monotonically increasing watermark equal to the
 * highest event timestamp observed so far. Every incoming event is compared to the
 * device watermark:
 * <ul>
 *     <li>timestamp &gt;= watermark -&gt; {@code ON_TIME} (and the watermark advances)</li>
 *     <li>timestamp &lt; watermark but within {@code allowedLateness} -&gt; {@code OUT_OF_ORDER}</li>
 *     <li>timestamp older than {@code allowedLateness} behind the watermark -&gt; {@code LATE}</li>
 * </ul>
 *
 * <p>This keeps lateness handling explicit and per-device, and decoupled from the
 * device-specific normalization strategies.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
@Component
public class WatermarkLateArrivalPolicy implements LateArrivalPolicy {

    private final Duration allowedLateness;
    private final Map<String, Instant> watermarks = new ConcurrentHashMap<>();

    /**
     * Creates the policy with the configured tolerance window.
     *
     * @param allowedLateness how far behind the watermark an event may be before it is
     *                        flagged {@code LATE} (property {@code ingestion.allowed-lateness},
     *                        default {@code PT30S})
     */
    public WatermarkLateArrivalPolicy(
            @Value("${ingestion.allowed-lateness:PT30S}") Duration allowedLateness) {
        this.allowedLateness = allowedLateness;
    }

    /**
     * Advances the device watermark and classifies the event as {@code ON_TIME},
     * {@code OUT_OF_ORDER} or {@code LATE} relative to that watermark.
     *
     * @param event the normalized event to classify
     * @return a copy of the event stamped with its lateness classification
     */
    @Override
    public NormalizedTelemetryEvent evaluate(NormalizedTelemetryEvent event) {
        Instant eventTimestamp = event.timestamp();

        Instant watermark = watermarks.merge(
                event.deviceId(),
                eventTimestamp,
                (current, incoming) -> incoming.isAfter(current) ? incoming : current);

        LatenessClassification classification;
        if (!eventTimestamp.isBefore(watermark)) {
            classification = LatenessClassification.ON_TIME;
        } else if (Duration.between(eventTimestamp, watermark).compareTo(allowedLateness) <= 0) {
            classification = LatenessClassification.OUT_OF_ORDER;
        } else {
            classification = LatenessClassification.LATE;
        }

        return event.withLateness(classification);
    }
}
