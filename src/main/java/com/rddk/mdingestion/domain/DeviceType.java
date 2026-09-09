package com.rddk.mdingestion.domain;

/**
 * Categories of devices supported by the ingestion pipeline. The value binds an
 * incoming request to the matching {@code DeviceIngestionStrategy}.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
public enum DeviceType {

    /**
     * Simple numeric device that emits a single reading per message (e.g. temperature).
     */
    NUMERIC,

    /**
     * Bursty device that emits an array of samples per message (e.g. vibration).
     */
    BURSTY
}
