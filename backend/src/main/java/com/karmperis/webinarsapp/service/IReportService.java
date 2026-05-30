package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.dto.JobStatusDTO;

/**
 * Service contract for generating and tracking report jobs.
 */
public interface IReportService {
    /**
     * Start an asynchronous report generation job.
     *
     * @param jobId      the job identifier
     * @param reportType the report type to generate
     */
    void generateReport(String jobId, String reportType);

    /**
     * Retrieve the current status of a report job.
     *
     * @param jobId the job identifier
     * @return the current job status DTO
     */
    JobStatusDTO getJobStatus(String jobId);
}