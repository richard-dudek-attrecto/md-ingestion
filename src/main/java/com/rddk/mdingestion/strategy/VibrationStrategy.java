package com.rddk.mdingestion.strategy;

import com.rddk.mdingestion.domain.DeviceType;
import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;
import com.rddk.mdingestion.dto.VibrationTelemetryDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * {@link DeviceIngestionStrategy} for bursty vibration devices. Fans out each sample of
 * the burst into its own {@link NormalizedTelemetryEvent}, deriving a per-sample timestamp
 * so downstream processing treats bursty and numeric data uniformly.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
@Component
public class VibrationStrategy implements DeviceIngestionStrategy<VibrationTelemetryDto> {

    /**
     * {@inheritDoc}
     *
     * @return always {@link DeviceType#BURSTY}
     */
    @Override
    public DeviceType getSupportedType() {
        return DeviceType.BURSTY;
    }

    /**
     * Expands the sample array into one normalized event per sample. Each event receives a
     * timestamp of {@code recordedAt + index * 100ms} and {@code sampleIndex},
     * {@code sampleValue} and {@code sampleCount} metrics.
     *
     * @param rawData the vibration payload; must not be {@code null}
     * @return one event per sample, or an empty list when there are no samples
     */
    @Override
    public List<NormalizedTelemetryEvent> normalize(VibrationTelemetryDto rawData) {
        Objects.requireNonNull(rawData, "rawData must not be null");

        List<Double> samples = rawData.samples() == null ? List.of() : rawData.samples();
        Instant ingestedAt = Instant.now();
        List<NormalizedTelemetryEvent> normalizedEvents = new ArrayList<>(samples.size());

        for (int index = 0; index < samples.size(); index++) {
            double sampleValue = samples.get(index);
            Instant sampleTimestamp = rawData.recordedAt().plusMillis(index * 100L);

            Map<String, Object> metrics = new LinkedHashMap<>();
            metrics.put("sampleIndex", index);
            metrics.put("sampleValue", sampleValue);
            metrics.put("sampleCount", samples.size());

            normalizedEvents.add(new NormalizedTelemetryEvent(
                    UUID.randomUUID().toString(),
                    rawData.deviceId(),
                    getSupportedType(),
                    sampleTimestamp,
                    ingestedAt,
                    metrics,
                    false
            ));
        }

        return normalizedEvents;
    }
}
