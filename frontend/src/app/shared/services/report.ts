import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { JobStatus } from '../interfaces/job-status';

/**
 * Service responsible for report-related API operations.
 */

@Injectable({
  providedIn: 'root',
})
export class Report {
  private readonly http = inject(HttpClient);
  private readonly reportsUrl = `${environment.apiUrl}/reports`;

  /**
   * Starts asynchronous report generation.
   *
   * @param type report type
   * @returns observable containing the created job status
   */
  generateReport(type: string): Observable<JobStatus> {
    return this.http.post<JobStatus>(`${this.reportsUrl}/generate?type=${type}`, {});
  }

  /**
   * Retrieves the current report job status.
   *
   * @param jobId job identifier
   * @returns observable containing the job status
   */
  getReport(jobId: string): Observable<JobStatus> {
    return this.http.get<JobStatus>(`${this.reportsUrl}/report/${jobId}`);
  }
}