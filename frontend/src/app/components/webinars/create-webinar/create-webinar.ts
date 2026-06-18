import { Component, inject, OnInit, signal } from '@angular/core';

import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { Webinar } from '../../../shared/services/webinar';
import { Navbar } from '../../layout/navbar/navbar';

/**
 * Component responsible for creating new webinars.
 */
@Component({
  selector: 'app-create-webinar',
  imports: [ReactiveFormsModule, RouterLink, Navbar],
  templateUrl: './create-webinar.html',
})
export class CreateWebinar implements OnInit {
  private readonly webinarService = inject(Webinar);

  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
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

  ngOnInit(): void {
    this.webinarForm.valueChanges.subscribe(() => {
      this.errorMessage.set(null);
    });
  }

  /**
   * Submits the webinar form and creates a new webinar.
   */
  onSubmit(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.webinarForm.invalid) {
      this.webinarForm.markAllAsTouched();
      return;
    }

    if (!this.webinarForm.dirty) {
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

    this.webinarService
      .createWebinar(webinarInsert)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set('Το σεμινάριο δημιουργήθηκε επιτυχώς.');
          this.webinarForm.reset({
            title: '',
            description: '',
            scheduledDate: '',
            duration: 60,
          });
          this.webinarForm.markAsPristine();

          setTimeout(() => {
            this.successMessage.set(null);
          }, 2000);
        },
        error: (error) => {
          console.error('Failed to create webinar', error);
          if (error.status === 409) {
            this.errorMessage.set('Υπάρχει ήδη σεμινάριο με αυτόν τον τίτλο.');
            return;
          }

          this.errorMessage.set('Η δημιουργία του σεμιναρίου απέτυχε.');
        },
      });
  }
}