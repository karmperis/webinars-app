package com.karmperis.webinarsapp.api;

import com.karmperis.webinarsapp.dto.JobStatusDTO;
import com.karmperis.webinarsapp.service.IReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReportRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReportService reportService;

    @MockitoBean
    private com.karmperis.webinarsapp.authentication.JwtService jwtService;

    @MockitoBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("POST /api/v1/reports/generate - Should return 202 Accepted")
    void startReport_ReturnsAccepted() throws Exception {
        String type = "popularity";

        mockMvc.perform(post("/api/v1/reports/generate")
                        .param("type", type)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.jobId").exists());
        verify(reportService).generateReport(anyString(), eq(type));
    }

    @Test
    @DisplayName("GET /api/v1/reports/report/{jobId} - Should return 200 OK when found")
    void getReport_ReturnsOk() throws Exception {
        String jobId = "test-job-id";
        JobStatusDTO statusDTO = JobStatusDTO.withoutData(jobId, "COMPLETED");

        when(reportService.getJobStatus(jobId)).thenReturn(statusDTO);

        mockMvc.perform(get("/api/v1/reports/report/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /api/v1/reports/generate - Should return 400 Bad Request when type parameter is missing")
    void startReport_WithMissingTypeParam_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/reports/generate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/reports/report/{jobId} - Should return 404 when job not found")
    void getReport_ReturnsNotFound() throws Exception {
        String jobId = "non-existent-id";

        when(reportService.getJobStatus(jobId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/reports/report/{jobId}", jobId))
                .andExpect(status().isNotFound());
    }
}