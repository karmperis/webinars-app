package com.karmperis.webinarsapp.service;

import com.karmperis.webinarsapp.dto.JobStatusDTO;
import com.karmperis.webinarsapp.dto.WebinarReportView;
import com.karmperis.webinarsapp.repository.WebinarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for generating webinar reports asynchronously and tracking job status.
 * The service starts an asynchronous report generation job for the given report type and
 * stores the job progress and result in an in-memory concurrent map. Supported report types:
 * <code>popularity</code>, <code>productive</code>, <code>inactive</code>.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService implements IReportService {
    private final Map<String, JobStatusDTO> jobStatusMap = new ConcurrentHashMap<>();
    private final WebinarRepository webinarRepository;

    /**
     * Starts asynchronous generation of a report for the specified report type.
     * <p>
     * This method runs asynchronously and updates an internal job status map. On success the job
     * status is set to COMPLETED and the generated report is stored inside the JobStatusDTO.
     * On failure the job status is set to FAILED and a RuntimeException is thrown.
     *
     * @param jobId      unique identifier for the background job
     * @param reportType type of report to generate (case-insensitive). Supported values:
     *                   "popularity", "productive", "inactive"
     */
    @Async
    @Transactional(readOnly = true)
    @Override
    public void generateReport(String jobId, String reportType) {
        jobStatusMap.put(jobId, JobStatusDTO.withoutData(jobId, "IN_PROGRESS"));

        try {
            List<WebinarReportView> report = switch (reportType.toLowerCase()) {
                case "popularity" -> webinarRepository.findWebinarsPopularityReport();
                case "productive" -> webinarRepository.findProductiveOrganizersReport();
                case "inactive" -> webinarRepository.findInactiveRecordsReport();
                default -> throw new IllegalArgumentException("Unknown report type: " + reportType);
            };
            jobStatusMap.put(jobId, new JobStatusDTO(jobId, "COMPLETED", report));
            log.info("Report '{}' generated for jobId={}, records={}", reportType, jobId, report.size());

        } catch (Exception e) {
            jobStatusMap.put(jobId, JobStatusDTO.withoutData(jobId, "FAILED"));
            log.error("Failed to generate report '{}' for jobId={}", reportType, jobId, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the current job status for the given job id.
     *
     * @param jobId identifier of the job
     * @return JobStatusDTO containing status and optional report data, or null if no such job exists
     */
    @Override
    public JobStatusDTO getJobStatus(String jobId) {
        return jobStatusMap.get(jobId);
    }
}