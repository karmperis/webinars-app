import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { Navbar } from '../../layout/navbar/navbar';
import { Report } from '../../../shared/services/report';
import { JobStatus } from '../../../shared/interfaces/job-status';

/**
 * Component responsible for generating and viewing reports.
 */

@Component({
  selector: 'app-reports',
  imports: [ReactiveFormsModule, Navbar],
  templateUrl: './reports.html',
})
export class Reports {
  private readonly reportService = inject(Report);

  readonly reportResult = signal<JobStatus | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly isLoading = signal(false);
  readonly selectedReportType = signal<string>('popularity');

  /**
   * Reactive form used to select the report type.
   */
  reportForm = new FormGroup({
    type: new FormControl('popularity', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  /**
   * Starts report generation and immediately requests its status.
   */
  onSubmit(): void {
    this.errorMessage.set(null);
    this.reportResult.set(null);

    if (this.reportForm.invalid) {
      this.reportForm.markAllAsTouched();
      return;
    }
    const reportType = this.reportForm.getRawValue().type;
    this.selectedReportType.set(reportType);
    this.isLoading.set(true);

    this.reportService.generateReport(reportType).subscribe({
      next: (job) => {
        this.loadReport(job.jobId);
      },
      error: (error) => {
        console.error('Failed to generate report', error);
        this.errorMessage.set('Η δημιουργία αναφοράς απέτυχε.');
        this.isLoading.set(false);
      },
    });
  }

  /**
   * Loads the generated report using the job identifier.
   *
   * @param jobId report job identifier
   */
  private loadReport(jobId: string): void {
    this.reportService
      .getReport(jobId)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (report) => {
          this.reportResult.set(report);
        },
        error: (error) => {
          console.error('Failed to load report', error);
          this.errorMessage.set('Η φόρτωση της αναφοράς απέτυχε.');
        },
      });
  }
}