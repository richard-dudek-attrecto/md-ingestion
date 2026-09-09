package com.rddk.mdingestion.controller;

import com.rddk.mdingestion.domain.DeviceType;
import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;
import com.rddk.mdingestion.dto.TemperatureTelemetryDto;
import com.rddk.mdingestion.dto.VibrationTelemetryDto;
import com.rddk.mdingestion.pipeline.IngestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Write-side of the pipeline. Accepts raw device payloads and delegates them to the
 * ingestion pipeline via {@link IngestionService}.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
@RestController
@RequestMapping("/api/v1")
public class IngestionController {

    private final IngestionService ingestionService;

    /**
     * Creates the controller backed by the ingestion service.
     *
     * @param ingestionService the pipeline orchestrator
     */
    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Ingests a temperature reading from a numeric device.
     *
     * @param rawData the validated temperature payload
     * @return {@code 200 OK} with the normalized events
     */
    @PostMapping("/ingest/temperature")
    public ResponseEntity<List<NormalizedTelemetryEvent>> ingestTemperature(
            @Valid @RequestBody TemperatureTelemetryDto rawData
    ) {
        return ResponseEntity.ok(ingestionService.ingest(DeviceType.NUMERIC, rawData));
    }

    /**
     * Ingests a burst of samples from a bursty vibration device.
     *
     * @param rawData the validated vibration payload
     * @return {@code 200 OK} with the normalized events (one per sample)
     */
    @PostMapping("/ingest/vibration")
    public ResponseEntity<List<NormalizedTelemetryEvent>> ingestVibration(
            @Valid @RequestBody VibrationTelemetryDto rawData
    ) {
        return ResponseEntity.ok(ingestionService.ingest(DeviceType.BURSTY, rawData));
    }
}
