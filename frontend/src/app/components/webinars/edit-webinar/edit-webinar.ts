import { Component, inject, OnInit, signal } from '@angular/core';

import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Navbar } from '../../layout/navbar/navbar';
import { Webinar } from '../../../shared/services/webinar';

/**
 * Component responsible for editing an existing webinar.
 */
@Component({
  selector: 'app-edit-webinar',
  imports: [ReactiveFormsModule, RouterLink, Navbar],
  templateUrl: './edit-webinar.html',
  styleUrl: './edit-webinar.css',
})
export class EditWebinar implements OnInit {
  private readonly webinarService = inject(Webinar);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private webinarUuid = '';

  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);

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
  }

  /**
   * Loads the selected webinar and fills the edit form.
   */
  private loadWebinar(): void {
    this.webinarService.getWebinarByUuid(this.webinarUuid).subscribe({
      next: (webinar) => {
        this.webinarForm.patchValue({
          title: webinar.title,
          description: webinar.description,
          scheduledDate: this.toDateTimeLocalValue(webinar.scheduledDate),
          duration: webinar.duration,
        });

        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to load webinar', error);
        this.errorMessage.set('Απέτυχε η φόρτωση του σεμιναρίου.');
        this.isLoading.set(false);
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

    this.webinarService.updateWebinar(this.webinarUuid, webinarEdit).subscribe({
      next: () => {
        this.router.navigate(['/webinars']);
      },
      error: (error) => {
        console.error('Failed to update webinar', error);
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