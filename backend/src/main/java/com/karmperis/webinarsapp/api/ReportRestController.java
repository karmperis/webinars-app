package com.karmperis.webinarsapp.api;

import com.karmperis.webinarsapp.dto.ErrorResponseDTO;
import com.karmperis.webinarsapp.dto.JobStatusDTO;
import com.karmperis.webinarsapp.service.IReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportRestController {
    private final IReportService reportService;

    @Operation(
            summary = "Start report generation",
            description = "Starts the asynchronous generation of a report based on the requested type: popularity, productive, inactive)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Report generation started successfully (IN_PROGRESS)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JobStatusDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid report type or missing parameter",
                    content = @Content(
                            mediaType = "application/json"
                            , schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
    })
    @PostMapping("/generate")
    public ResponseEntity<JobStatusDTO> startReport(@RequestParam String type) {
        String jobId = UUID.randomUUID().toString();
        reportService.generateReport(jobId, type);
        return ResponseEntity.accepted().body(JobStatusDTO.withoutData(jobId, "IN_PROGRESS"));
    }

    @Operation(
            summary = "Check report status",
            description = "Checks the status of an asynchronous report job using its unique job ID. Returns the data if completed."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Job status retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JobStatusDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Job ID not found",
                    content = @Content(
                            mediaType = "application/json"
                            , schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json"
                            , schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/report/{jobId}")
    public ResponseEntity<JobStatusDTO> getReport(@PathVariable String jobId) {
        JobStatusDTO status = reportService.getJobStatus(jobId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }
}