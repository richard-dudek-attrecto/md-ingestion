package com.rddk.mdingestion.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests covering the HTTP ingestion endpoints and the inspection read path.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
@SpringBootTest
class IngestionFlowIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    /**
     * Creates a MockMvc instance configured for the Spring application context.
     *
     * @return a MockMvc instance bound to the web application context
     */
    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    /**
     * Verifies that a vibration burst is ingested, normalized, and becomes available for inspection.
     *
     * @throws Exception when the mocked HTTP request fails
     */
    @Test
    void ingestedVibrationBurstBecomesAvailableDownstream() throws Exception {
        MockMvc mockMvc = mockMvc();

        String payload = """
                {"deviceId":"vib-42","samples":[0.1,0.2,0.3,0.4],"recordedAt":"2026-01-01T00:00:00Z"}
                """;

        mockMvc.perform(post("/api/v1/ingest/vibration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].deviceType").value("BURSTY"));

        mockMvc.perform(get("/api/v1/events").param("deviceId", "vib-42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].metrics.lateness").exists());
    }

    /**
     * Verifies malformed temperature payloads are rejected with a 400 response.
     *
     * @throws Exception when the mocked HTTP request fails
     */
    @Test
    void invalidTemperaturePayloadIsRejected() throws Exception {
        String payload = """
                {"temperatureValue":21.5,"recordedAt":"2026-01-01T00:00:00Z"}
                """;

        mockMvc().perform(post("/api/v1/ingest/temperature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
