package com.rddk.mdingestion.strategy;

import com.rddk.mdingestion.domain.DeviceType;
import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry that collects every {@link DeviceIngestionStrategy} bean and resolves the
 * correct one by {@link DeviceType}. Adding a new strategy bean automatically registers
 * it here, so existing code stays untouched (Open-Closed).
 *
 * @author richard.dudek
 * @since 0.0.1
 */
@Service
public class IngestionStrategyRegistry {

    private final Map<DeviceType, DeviceIngestionStrategy<?>> strategies;

    /**
     * Indexes the injected strategies by their supported device type.
     *
     * @param strategies all {@link DeviceIngestionStrategy} beans discovered by Spring
     */
    public IngestionStrategyRegistry(List<DeviceIngestionStrategy<?>> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(
                        DeviceIngestionStrategy::getSupportedType,
                        Function.identity(),
                        (existing, replacement) -> replacement,
                        () -> new EnumMap<>(DeviceType.class)
                ));
    }

    /**
     * Resolves the strategy for the given device type and normalizes the raw payload.
     *
     * @param deviceType the device type whose strategy should handle the payload
     * @param rawData    the raw device payload
     * @param <T>        the raw payload type
     * @return the normalized events produced by the matching strategy
     * @throws IllegalArgumentException if no strategy is registered for {@code deviceType}
     */
    public <T> List<NormalizedTelemetryEvent> normalize(DeviceType deviceType, T rawData) {
        @SuppressWarnings("unchecked")
        DeviceIngestionStrategy<T> strategy = (DeviceIngestionStrategy<T>) strategies.get(deviceType);

        if (strategy == null) {
            throw new IllegalArgumentException("No ingestion strategy found for device type: " + deviceType);
        }

        return strategy.normalize(rawData);
    }
}
