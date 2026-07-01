import { Component, inject, OnInit, signal } from '@angular/core';

import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';
import { Router, RouterLink } from '@angular/router';

import { Navbar } from '../../layout/navbar/navbar';

import { Auth } from '../../../shared/services/auth';
import { Webinar } from '../../../shared/services/webinar';

import { WebinarReadOnly } from '../../../shared/interfaces/webinar-read-only';

/**
 * Component responsible for displaying webinars organized
 * by the currently authenticated user.
 */
@Component({
  selector: 'app-organizer-webinars',
  imports: [Navbar, DatePipe, RouterLink],
  templateUrl: './organizer-webinars.html',
})
export class OrganizerWebinars implements OnInit {
  private readonly webinarService = inject(Webinar);
  private readonly authService = inject(Auth);
  private readonly router = inject(Router);

  readonly webinars = signal<WebinarReadOnly[]>([]);
  readonly isLoading = signal(true);
  readonly loadError = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(5);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly sortField = signal('scheduledDate');
  readonly sortDirection = signal<'asc' | 'desc'>('asc');
  readonly actionErrorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadOrganizerWebinars();
  }

  /**
   * Loads webinars organized by the current user.
   */
  private loadOrganizerWebinars(): void {
    const userUuid = this.authService.getCurrentUserUuid();

    if (!userUuid) {
      this.loadError.set('Δεν ήταν δυνατή η αναγνώριση του συνδεδεμένου χρήστη.');
      this.isLoading.set(false);
      return;
    }

    this.isLoading.set(true);
    this.loadError.set(null);

    this.webinarService
      .getWebinarsByOrganizer(
        userUuid,
        this.currentPage(),
        this.pageSize(),
        this.sortField(),
        this.sortDirection(),
      )
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (page) => {
          this.webinars.set(page.content ?? []);
          this.totalPages.set(page.totalPages ?? 0);
          this.totalElements.set(page.totalElements ?? 0);
        },
        error: (error) => {
          console.error('Failed to load organizer webinars', error);
          this.loadError.set('Απέτυχε η φόρτωση των σεμιναρίων διοργάνωσης.');
        },
      });
  }

  /**
   * Checks whether the current user can edit organizer webinars.
   *
   * @returns true if the user has edit webinar capability
   */
  canEditWebinar(): boolean {
    return this.authService.hasCapability('EDIT_WEBINAR');
  }

  /**
   * Checks whether the current user can delete organizer webinars.
   *
   * @returns true if the user has delete webinar capability
   */
  canDeleteWebinar(): boolean {
    return this.authService.hasCapability('DELETE_WEBINAR');
  }

  /**
   * Navigates to the webinar edit page.
   *
   * @param uuid webinar UUID
   */
  editWebinar(uuid: string): void {
    this.router.navigate(['/webinars', uuid, 'edit']);
  }

  /**
   * Deletes an organizer webinar and refreshes the displayed list.
   *
   * @param uuid webinar UUID
   */
  deleteWebinar(uuid: string): void {
    if (!confirm('Είστε σίγουροι ότι θέλετε να διαγράψετε αυτό το σεμινάριο;')) {
      return;
    }

    this.webinarService.deleteWebinar(uuid).subscribe({
      next: () => {
        this.successMessage.set('Το σεμινάριο διαγράφηκε επιτυχώς.');

        setTimeout(() => {
          this.successMessage.set(null);
        }, 2000);

        if (this.webinars().length === 1 && this.currentPage() > 0) {
          this.currentPage.update((page) => page - 1);
        }

        this.loadOrganizerWebinars();
      },
      error: (error) => {
        console.error('Failed to delete organizer webinar', error);
        this.actionErrorMessage.set('Η διαγραφή του σεμιναρίου απέτυχε.');

        setTimeout(() => {
          this.actionErrorMessage.set(null);
        }, 2000);
      },
    });
  }

  /**
   * Loads the previous page of organizer webinars.
   */
  previousPage(): void {
    if (this.currentPage() === 0) {
      return;
    }

    this.currentPage.update((page) => page - 1);
    this.loadOrganizerWebinars();
  }

  /**
   * Loads the next page of organizer webinars.
   */
  nextPage(): void {
    if (this.currentPage() >= this.totalPages() - 1) {
      return;
    }

    this.currentPage.update((page) => page + 1);
    this.loadOrganizerWebinars();
  }

  /**
   * Updates the current sorting option and reloads organizer webinars from the first page.
   *
   * @param value selected sorting option in field,direction format
   */
  onSortChange(value: string): void {
    const [field, direction] = value.split(',');

    this.sortField.set(field);
    this.sortDirection.set(direction as 'asc' | 'desc');
    this.currentPage.set(0);
    this.loadOrganizerWebinars();
  }
}