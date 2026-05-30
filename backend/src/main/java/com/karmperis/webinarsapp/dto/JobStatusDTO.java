package com.karmperis.webinarsapp.dto;

import java.util.List;

/**
 * DTO representing the status of a background job with optional result data.
 *
 * @param jobId  the job identifier
 * @param status the current job status
 * @param data   the job result data, or {@code null} if not available
 */
public record JobStatusDTO<T>(
        String jobId,
        String status,
        List<T> data
) {
    /**
     * Create a job status response without result data.
     *
     * @param jobId  the job identifier
     * @param status the current job status
     * @param <T>    the data element type
     * @return a job status DTO with no data payload
     */
    public static <T> JobStatusDTO<T> withoutData(String jobId, String status) {
        return new JobStatusDTO<>(jobId, status, null);
    }
}