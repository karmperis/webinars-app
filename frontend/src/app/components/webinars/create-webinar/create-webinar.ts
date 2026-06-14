import { Component, inject, signal } from '@angular/core';

import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { Webinar } from '../../../shared/services/webinar';
import { Navbar } from '../../layout/navbar/navbar';

/**
 * Component responsible for creating new webinars.
 */
@Component({
  selector: 'app-create-webinar',
  imports: [ReactiveFormsModule, RouterLink, Navbar],
  templateUrl: './create-webinar.html',
  styleUrl: './create-webinar.css',
})
export class CreateWebinar {
  private readonly webinarService = inject(Webinar);
  private readonly router = inject(Router);

  readonly errorMessage = signal<string | null>(null);
  readonly isSubmitting = signal(false);

  /**
   * Reactive form based on the backend WebinarInsertDTO.
   */
  webinarForm = new FormGroup({
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(5), Validators.maxLength(100)],
    }),
    description: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(1000)],
    }),
    scheduledDate: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    duration: new FormControl(60, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(15), Validators.max(480)],
    }),
  });

  /**
   * Submits the webinar form and creates a new webinar.
   */
  onSubmit(): void {
    this.errorMessage.set(null);

    if (this.webinarForm.invalid) {
      this.webinarForm.markAllAsTouched();
      return;
    }

    const formValue = this.webinarForm.getRawValue();
    const scheduledDate = new Date(formValue.scheduledDate);

    if (scheduledDate <= new Date()) {
      this.errorMessage.set(
        'Η ημερομηνία και ώρα διεξαγωγής του σεμιναρίου πρέπει να είναι μεταγενέστερη από την τρέχουσα ημερομηνία και ώρα.',
      );
      return;
    }
    const webinarInsert = {
      ...formValue,
      scheduledDate: scheduledDate.toISOString(),
    };

    this.isSubmitting.set(true);

    this.webinarService.createWebinar(webinarInsert).subscribe({
      next: () => {
        this.router.navigate(['/webinars']);
      },
      error: () => {
        this.errorMessage.set('Η δημιουργία του σεμιναρίου απέτυχε.');
        this.isSubmitting.set(false);
      },
    });
  }
}