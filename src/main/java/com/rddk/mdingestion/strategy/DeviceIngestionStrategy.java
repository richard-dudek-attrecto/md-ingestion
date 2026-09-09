package com.rddk.mdingestion.strategy;

import com.rddk.mdingestion.domain.DeviceType;
import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;

import java.util.List;

/**
 * Strategy contract for turning a device-specific raw payload into the unified
 * {@link NormalizedTelemetryEvent} model. One implementation exists per device type,
 * enabling new devices to be added without modifying existing code (Open-Closed).
 *
 * @param <T> the raw device payload type handled by this strategy
 * @author richard.dudek
 * @since 0.0.1
 */
public interface DeviceIngestionStrategy<T> {

    /**
     * Returns the device type this strategy is responsible for.
     *
     * @return the supported {@link DeviceType}
     */
    DeviceType getSupportedType();

    /**
     * Normalizes a raw device payload into one or more unified events.
     *
     * @param rawData the device-specific payload to normalize
     * @return the resulting normalized events (may contain more than one for bursty devices)
     */
    List<NormalizedTelemetryEvent> normalize(T rawData);
}
