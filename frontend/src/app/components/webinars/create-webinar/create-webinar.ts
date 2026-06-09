import { Component, inject } from '@angular/core';

import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { Webinar } from '../../../shared/services/webinar';

/**
 * Component responsible for creating new webinars.
 */
@Component({
  selector: 'app-create-webinar',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './create-webinar.html',
  styleUrl: './create-webinar.css',
})
export class CreateWebinar {
  private readonly webinarService = inject(Webinar);
  private readonly router = inject(Router);

  errorMessage = '';

  /**
   * Reactive form based on the backend WebinarInsertDTO.
   */
  webinarForm = new FormGroup({
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(100)],
    }),
    description: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(500)],
    }),
    scheduledDate: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    duration: new FormControl(60, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(1)],
    }),
  });

  /**
   * Submits the webinar form and creates a new webinar.
   */
  onSubmit(): void {
    this.errorMessage = '';

    if (this.webinarForm.invalid) {
      this.webinarForm.markAllAsTouched();
      return;
    }

    this.webinarService.createWebinar(this.webinarForm.getRawValue()).subscribe({
      next: () => {
        this.router.navigate(['/webinars']);
      },
      error: () => {
        this.errorMessage = 'Η δημιουργία του webinar απέτυχε.';
      },
    });
  }
}