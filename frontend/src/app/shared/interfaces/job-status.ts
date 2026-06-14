import { WebinarReportView } from './webinar-report-view';

/**
 * Represents the status of an asynchronous report generation job.
 */
export interface JobStatus {
  jobId: string;
  status: 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  data: WebinarReportView[] | null;
}