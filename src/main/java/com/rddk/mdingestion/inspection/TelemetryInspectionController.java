package com.rddk.mdingestion.inspection;

import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only endpoints that expose the buffered telemetry for the browser test console.
 *
 * <p><strong>Testing / demo only.</strong> These endpoints exist to visualise the result
 * of ingestion (including per-event lateness) from a browser. They are not part of the
 * production downstream contract; a real consumer would read from the
 * {@link com.rddk.mdingestion.pipeline.TelemetrySink} instead.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
@RestController
@RequestMapping("/api/v1/events")
public class TelemetryInspectionController {

    private final TelemetryInspectionService inspectionService;

    /**
     * Creates the controller backed by the inspection service.
     *
     * @param inspectionService the read-only inspection helper
     */
    public TelemetryInspectionController(TelemetryInspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    /**
     * Returns buffered normalized events, optionally filtered by device
     * (testing/visualisation only).
     *
     * @param deviceId optional device identifier to filter by; when omitted all events are returned
     * @return {@code 200 OK} with the matching events
     */
    @GetMapping
    public ResponseEntity<List<NormalizedTelemetryEvent>> events(
            @RequestParam(name = "deviceId", required = false) String deviceId
    ) {
        return ResponseEntity.ok(
                deviceId == null ? inspectionService.all() : inspectionService.byDevice(deviceId));
    }

    /**
     * Returns only the events classified as late (testing/visualisation only).
     *
     * @return {@code 200 OK} with the late events
     */
    @GetMapping("/late")
    public ResponseEntity<List<NormalizedTelemetryEvent>> lateEvents() {
        return ResponseEntity.ok(inspectionService.late());
    }
}
