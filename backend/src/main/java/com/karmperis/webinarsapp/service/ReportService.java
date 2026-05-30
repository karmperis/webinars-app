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

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService implements IReportService {
    private final Map<String, JobStatusDTO> jobStatusMap = new ConcurrentHashMap<>();
    private final WebinarRepository webinarRepository;

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

    @Override
    public JobStatusDTO getJobStatus(String jobId) {
        return jobStatusMap.get(jobId);
    }
}