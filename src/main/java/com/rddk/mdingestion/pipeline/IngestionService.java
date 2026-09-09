package com.rddk.mdingestion.pipeline;

import com.rddk.mdingestion.domain.DeviceType;
import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;
import com.rddk.mdingestion.strategy.IngestionStrategyRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Coordinates the ingestion pipeline for a single raw device payload:
 * <ol>
 *     <li>normalize the device-specific payload via the matching strategy</li>
 *     <li>classify each event for lateness/out-of-order via the {@link LateArrivalPolicy}</li>
 *     <li>publish the results to the downstream {@link TelemetrySink}</li>
 * </ol>
 *
 * @author richard.dudek
 * @since 0.0.1
 */
@Service
public class IngestionService {

    private final IngestionStrategyRegistry strategyRegistry;
    private final LateArrivalPolicy lateArrivalPolicy;
    private final TelemetrySink telemetrySink;

    /**
     * Creates the service with its collaborating pipeline components.
     *
     * @param strategyRegistry  resolves the normalization strategy per device type
     * @param lateArrivalPolicy classifies events for lateness/out-of-order
     * @param telemetrySink     downstream sink the classified events are published to
     */
    public IngestionService(IngestionStrategyRegistry strategyRegistry,
                            LateArrivalPolicy lateArrivalPolicy,
                            TelemetrySink telemetrySink) {
        this.strategyRegistry = strategyRegistry;
        this.lateArrivalPolicy = lateArrivalPolicy;
        this.telemetrySink = telemetrySink;
    }

    /**
     * Runs the full pipeline for one raw payload: normalize, classify lateness, publish.
     *
     * @param deviceType the device type driving strategy selection
     * @param rawData    the raw device payload
     * @param <T>        the raw payload type
     * @return the normalized, lateness-classified events that were published
     */
    public <T> List<NormalizedTelemetryEvent> ingest(DeviceType deviceType, T rawData) {
        List<NormalizedTelemetryEvent> classified = strategyRegistry.normalize(deviceType, rawData).stream()
                .map(lateArrivalPolicy::evaluate)
                .toList();

        classified.forEach(telemetrySink::publish);
        return classified;
    }
}
