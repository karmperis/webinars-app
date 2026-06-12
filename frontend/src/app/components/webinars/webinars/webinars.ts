import { Component, inject, OnInit, signal } from '@angular/core';
import { Navbar } from '../../layout/navbar/navbar';
import { WebinarReadOnly } from '../../../shared/interfaces/webinar-read-only';

import { Webinar } from '../../../shared/services/webinar';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';
import { Router } from '@angular/router';
import { Auth } from '../../../shared/services/auth';

/**
 * Component responsible for displaying webinar-related functionality.
 */
@Component({
  selector: 'app-webinars',
  imports: [Navbar, DatePipe],
  templateUrl: './webinars.html',
  styleUrl: './webinars.css',
})
export class Webinars implements OnInit {
  private readonly webinarService = inject(Webinar);
  private readonly authService = inject(Auth);
  private readonly router = inject(Router);
  readonly webinars = signal<WebinarReadOnly[]>([]);
  readonly isLoading = signal(true);
  readonly loadError = signal<string | null>(null);

  ngOnInit(): void {
    this.loadWebinars();
  }

  /**
   * Checks whether the current user can manage webinars.
   *
   * @param webinar webinar to check
   * @returns true if the user is ADMIN or owns the webinar as organizer
   */
  canManageWebinar(webinar: WebinarReadOnly): boolean {
    const currentUserUuid = this.authService.getCurrentUserUuid();
    return this.authService.hasRole('ADMIN') || currentUserUuid === webinar.organizer.uuid;
  }

  /**
   * Checks whether the current user can enroll in webinars.
   *
   * @returns true for ADMIN and PARTICIPANT users
   */
  canEnrollInWebinars(): boolean {
    return this.authService.hasRole('ADMIN') || this.authService.hasRole('PARTICIPANT');
  }

  /**
   * Loads webinars from the backend API.
   */
  private loadWebinars(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.webinarService
      .getWebinars()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (page) => {
          this.webinars.set(page.content ?? []);
        },
        error: (error) => {
          console.error('Failed to load webinars', error);
          this.loadError.set('Απέτυχε η φόρτωση των σεμιναρίων.');
        },
      });
  }

  /**
   * Deletes a webinar and refreshes the displayed list.
   *
   * @param uuid webinar UUID
   */
  deleteWebinar(uuid: string): void {
    if (!confirm('Είστε σίγουροι ότι θέλετε να διαγράψετε αυτό το σεμινάριο;')) {
      return;
    }

    this.webinarService.deleteWebinar(uuid).subscribe({
      next: () => {
        this.loadWebinars();
      },
      error: (error) => {
        console.error('Failed to delete webinar', error);
        this.loadError.set('Η διαγραφή του σεμιναρίου απέτυχε.');
      },
    });
  }

  /**
   * Enrolls the current authenticated user in a webinar.
   *
   * @param webinarUuid webinar UUID
   */
  enrollInWebinar(webinarUuid: string): void {
    const userUuid = this.authService.getCurrentUserUuid();

    if (!userUuid) {
      this.loadError.set('Δεν ήταν δυνατή η αναγνώριση του συνδεδεμένου χρήστη.');
      return;
    }

    this.webinarService.enrollInWebinar(webinarUuid, userUuid).subscribe({
      next: () => {
        alert('Η εγγραφή στο σεμινάριο ολοκληρώθηκε με επιτυχία.');
        this.loadWebinars();
      },
      error: (error) => {
        console.error('Failed to enroll in webinar', error);
        if (error.status === 409) {
          this.loadError.set('Έχετε ήδη εγγραφεί σε αυτό το σεμινάριο.');

          setTimeout(() => {
            this.loadError.set(null);
            this.loadWebinars();
          }, 1000);
          return;
        }
        this.loadError.set('Η εγγραφή στο σεμινάριο απέτυχε.');
      },
    });
  }

  /**
   * Navigates to the webinar edit page.
   *
   * @param uuid webinar UUID
   */
  editWebinar(uuid: string): void {
    this.router.navigate(['/webinars', uuid, 'edit']);
  }
}