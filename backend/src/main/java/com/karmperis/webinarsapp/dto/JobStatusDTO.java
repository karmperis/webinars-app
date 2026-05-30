package com.karmperis.webinarsapp.dto;

import java.util.List;

/**
 * DTO representing the status of a background job with optional report data.
 *
 * @param jobId  the job identifier
 * @param status the current job status
 * @param data   the report data, or {@code null} if not available
 */
public record JobStatusDTO(
        String jobId,
        String status,
        List<WebinarReportView> data
) {
    /**
     * Create a job status response without report data.
     *
     * @param jobId  the job identifier
     * @param status the current job status
     * @return a job status DTO with no data payload
     */
    public static JobStatusDTO withoutData(String jobId, String status) {
        return new JobStatusDTO(jobId, status, null);
    }
}