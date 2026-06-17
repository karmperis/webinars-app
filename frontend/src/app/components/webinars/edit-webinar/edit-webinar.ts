import { Component, inject, OnInit, signal } from '@angular/core';

import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { Navbar } from '../../layout/navbar/navbar';
import { Webinar } from '../../../shared/services/webinar';

/**
 * Component responsible for editing an existing webinar.
 */
@Component({
  selector: 'app-edit-webinar',
  imports: [ReactiveFormsModule, RouterLink, Navbar],
  templateUrl: './edit-webinar.html',
})
export class EditWebinar implements OnInit {
  private readonly webinarService = inject(Webinar);
  private readonly route = inject(ActivatedRoute);

  private webinarUuid = '';

  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly isSubmitting = signal(false);

  /**
   * Reactive form based on the backend WebinarEditDTO.
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
    this.webinarUuid = this.route.snapshot.paramMap.get('uuid') ?? '';

    if (!this.webinarUuid) {
      this.errorMessage.set('Δεν βρέθηκε το σεμινάριο.');
      this.isLoading.set(false);
      return;
    }

    this.loadWebinar();

    this.webinarForm.valueChanges.subscribe(() => {
      this.errorMessage.set(null);
    });
  }

  /**
   * Loads the selected webinar and fills the edit form.
   */
  private loadWebinar(): void {
    this.webinarService
      .getWebinarByUuid(this.webinarUuid)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (webinar) => {
          this.webinarForm.patchValue({
            title: webinar.title,
            description: webinar.description,
            scheduledDate: this.toDateTimeLocalValue(webinar.scheduledDate),
            duration: webinar.duration,
          });

          this.webinarForm.markAsPristine();
        },
        error: (error) => {
          console.error('Failed to load webinar', error);
          this.errorMessage.set('Απέτυχε η φόρτωση του σεμιναρίου.');
        },
      });
  }

  /**
   * Submits the form and updates the selected webinar.
   */
  onSubmit(): void {
    this.errorMessage.set(null);

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

    const webinarEdit = {
      ...formValue,
      scheduledDate: scheduledDate.toISOString(),
    };

    this.isSubmitting.set(true);

    this.webinarService
      .updateWebinar(this.webinarUuid, webinarEdit)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set('Το σεμινάριο ενημερώθηκε επιτυχώς.');
          this.webinarForm.markAsPristine();

          setTimeout(() => {
            this.successMessage.set(null);
          }, 2000);
        },
        error: (error) => {
          console.error('Failed to update webinar', error);
          if (error.status === 409) {
            this.errorMessage.set('Υπάρχει ήδη σεμινάριο με αυτόν τον τίτλο.');
            return;
          }
          this.errorMessage.set('Η ενημέρωση του σεμιναρίου απέτυχε.');
        },
      });
  }

  /**
   * Converts an ISO date string to a datetime-local input value.
   *
   * @param value ISO date value returned by the backend
   * @returns datetime-local compatible value
   */
  private toDateTimeLocalValue(value: string): string {
    const date = new Date(value);
    const timezoneOffsetInMs = date.getTimezoneOffset() * 60_000;
    const localDate = new Date(date.getTime() - timezoneOffsetInMs);
    return localDate.toISOString().slice(0, 16);
  }
}