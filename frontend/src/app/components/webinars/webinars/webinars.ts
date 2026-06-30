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
})
export class Webinars implements OnInit {
  private readonly webinarService = inject(Webinar);
  private readonly authService = inject(Auth);
  private readonly router = inject(Router);
  readonly webinars = signal<WebinarReadOnly[]>([]);
  readonly enrolledWebinarUuids = signal<Set<string>>(new Set());
  readonly isLoading = signal(true);
  readonly loadError = signal<string | null>(null);
  readonly actionErrorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(5);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly sortField = signal('scheduledDate');
  readonly sortDirection = signal<'asc' | 'desc'>('asc');

  ngOnInit(): void {
    this.loadWebinars();
    this.loadEnrolledWebinars();
  }

  /**
   * Displays a success message for a short period.
   *
   * @param message success message to display
   */
  private showSuccess(message: string): void {
    this.successMessage.set(message);

    setTimeout(() => {
      this.successMessage.set(null);
    }, 2000);
  }

  /**
   * Displays an error message for a short period.
   *
   * @param message error message to display
   */
  private showError(message: string): void {
    this.actionErrorMessage.set(message);

    setTimeout(() => {
      this.actionErrorMessage.set(null);
    }, 2000);
  }

  /**
   * Checks whether the current user can manage the given webinar.
   *
   * @param webinar webinar to check
   * @returns true if the user has manage permission for this webinar
   */
  canManageWebinar(webinar: WebinarReadOnly): boolean {
    const currentUserUuid = this.authService.getCurrentUserUuid();
    const isOwner = currentUserUuid === webinar.organizer.uuid;

    return this.authService.hasCapability('MANAGE_WEBINARS') || isOwner;
  }

  /**
   * Checks whether the current user can edit the given webinar.
   *
   * @param webinar webinar to check
   * @returns true if the user has edit permission for this webinar
   */
  canEditWebinar(webinar: WebinarReadOnly): boolean {
    return this.authService.hasCapability('EDIT_WEBINAR') && this.canManageWebinar(webinar);
  }

  /**
   * Checks whether the current user can delete the given webinar.
   *
   * @param webinar webinar to check
   * @returns true if the user has delete permission for this webinar
   */
  canDeleteWebinar(webinar: WebinarReadOnly): boolean {
    return this.authService.hasCapability('DELETE_WEBINAR') && this.canManageWebinar(webinar);
  }

  /**
   * Checks whether the current user can enroll in the given webinar.
   *
   * @param webinar webinar to check
   * @returns true if the user has enroll permission and backend business rules allow it
   */
  canEnrollInWebinar(webinar: WebinarReadOnly): boolean {
    const currentUserUuid = this.authService.getCurrentUserUuid();
    const isOwnWebinar = currentUserUuid === webinar.organizer.uuid;

    return this.authService.hasCapability('ENROLL_IN_WEBINAR') && !isOwnWebinar;
  }

  /**
   * Checks whether the current user is already enrolled in the given webinar.
   *
   * @param webinarUuid webinar UUID
   * @returns true if the current user is already enrolled
   */
  isAlreadyEnrolled(webinarUuid: string): boolean {
    return this.enrolledWebinarUuids().has(webinarUuid);
  }

  /**
   * Loads webinars from the backend API.
   */
  private loadWebinars(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.webinarService
      .getWebinars(this.currentPage(), this.pageSize(), this.sortField(), this.sortDirection())
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (page) => {
          this.webinars.set(page.content ?? []);
          this.totalPages.set(page.totalPages ?? 0);
          this.totalElements.set(page.totalElements ?? 0);
        },
        error: (error) => {
          console.error('Failed to load webinars', error);
          this.loadError.set('Απέτυχε η φόρτωση των σεμιναρίων.');
        },
      });
  }

  /**
   * Loads the previous page of webinars.
   */
  previousPage(): void {
    if (this.currentPage() === 0) {
      return;
    }

    this.currentPage.update((page) => page - 1);
    this.loadWebinars();
  }

  /**
   * Loads the next page of webinars.
   */
  nextPage(): void {
    if (this.currentPage() >= this.totalPages() - 1) {
      return;
    }

    this.currentPage.update((page) => page + 1);
    this.loadWebinars();
  }

  /**
   * Updates the current sorting option and reloads webinars from the first page.
   *
   * @param value selected sorting option in field,direction format
   */
  onSortChange(value: string): void {
    const [field, direction] = value.split(',');

    this.sortField.set(field);
    this.sortDirection.set(direction as 'asc' | 'desc');
    this.currentPage.set(0);
    this.loadWebinars();
  }

  /**
   * Loads the webinars where the current user is already enrolled.
   */
  private loadEnrolledWebinars(): void {
    const userUuid = this.authService.getCurrentUserUuid();

    if (!userUuid) {
      this.enrolledWebinarUuids.set(new Set());
      return;
    }

    this.webinarService.getWebinarsByParticipant(userUuid, 0, 100).subscribe({
      next: (page) => {
        const enrolledUuids = new Set((page.content ?? []).map((webinar) => webinar.uuid));
        this.enrolledWebinarUuids.set(enrolledUuids);
      },
      error: (error) => {
        console.error('Failed to load enrolled webinars', error);
        this.enrolledWebinarUuids.set(new Set());
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
        this.showSuccess('Το σεμινάριο διαγράφηκε επιτυχώς.');
        if (this.webinars().length === 1 && this.currentPage() > 0) {
          this.currentPage.update((page) => page - 1);
        }

        this.loadWebinars();
      },
      error: (error) => {
        console.error('Failed to delete webinar', error);
        this.showError('Η διαγραφή του σεμιναρίου απέτυχε.');
      },
    });
  }

  /**
   * Enrolls the current authenticated user in a webinar.
   *
   * @param webinarUuid webinar UUID
   */
  enrollInWebinar(webinarUuid: string): void {
    this.actionErrorMessage.set(null);
    this.successMessage.set(null);

    const userUuid = this.authService.getCurrentUserUuid();

    if (!userUuid) {
      this.showError('Δεν ήταν δυνατή η αναγνώριση του συνδεδεμένου χρήστη.');
      return;
    }

    this.webinarService.enrollInWebinar(webinarUuid, userUuid).subscribe({
      next: () => {
        this.showSuccess('Η εγγραφή στο σεμινάριο ολοκληρώθηκε με επιτυχία.');
        this.loadWebinars();
        this.loadEnrolledWebinars();
      },
      error: (error) => {
        console.error('Failed to enroll in webinar', error);

        if (error.status === 409) {
          this.showError('Έχετε ήδη εγγραφεί σε αυτό το σεμινάριο.');
          return;
        }

        if (error.status === 400) {
          this.showError('Δεν μπορείτε να εγγραφείτε σε σεμινάριο που διοργανώνετε εσείς.');
          return;
        }

        this.showError('Η εγγραφή στο σεμινάριο απέτυχε.');
      },
    });
  }

  /**
   * Unenrolls the current authenticated user from a webinar.
   *
   * @param webinarUuid webinar UUID
   */
  unenrollFromWebinar(webinarUuid: string): void {
    this.actionErrorMessage.set(null);
    this.successMessage.set(null);

    const userUuid = this.authService.getCurrentUserUuid();

    if (!userUuid) {
      this.showError('Δεν ήταν δυνατή η αναγνώριση του συνδεδεμένου χρήστη.');
      return;
    }

    this.webinarService.unenrollFromWebinar(webinarUuid, userUuid).subscribe({
      next: () => {
        this.showSuccess('Η απεγγραφή από το σεμινάριο ολοκληρώθηκε με επιτυχία.');

        const updatedEnrolledUuids = new Set(this.enrolledWebinarUuids());
        updatedEnrolledUuids.delete(webinarUuid);
        this.enrolledWebinarUuids.set(updatedEnrolledUuids);
      },
      error: (error) => {
        console.error('Failed to unenroll from webinar', error);

        if (error.status === 400) {
          this.showError('Δεν είστε εγγεγραμμένος σε αυτό το σεμινάριο.');
          return;
        }

        this.showError('Η απεγγραφή από το σεμινάριο απέτυχε.');
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