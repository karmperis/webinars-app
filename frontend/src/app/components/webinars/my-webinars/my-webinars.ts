import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { Navbar } from '../../layout/navbar/navbar';

import { Webinar } from '../../../shared/services/webinar';
import { Auth } from '../../../shared/services/auth';

import { WebinarReadOnly } from '../../../shared/interfaces/webinar-read-only';

import { finalize } from 'rxjs';

/**
 * Component responsible for displaying webinars where the current user
 * is enrolled as a participant.
 */
@Component({
  selector: 'app-my-webinars',
  imports: [Navbar, DatePipe, RouterLink],
  templateUrl: './my-webinars.html',
})
export class MyWebinars implements OnInit {
  private readonly webinarService = inject(Webinar);
  private readonly authService = inject(Auth);

  readonly webinars = signal<WebinarReadOnly[]>([]);
  readonly isLoading = signal(true);
  readonly loadError = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(5);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);

  ngOnInit(): void {
    this.loadMyWebinars();
  }

  /**
   * Loads webinars where the current user is enrolled.
   */
  private loadMyWebinars(): void {
    const userUuid = this.authService.getCurrentUserUuid();

    if (!userUuid) {
      this.loadError.set('Δεν ήταν δυνατή η αναγνώριση του συνδεδεμένου χρήστη.');
      this.isLoading.set(false);
      return;
    }

    this.isLoading.set(true);
    this.loadError.set(null);

    this.webinarService
      .getWebinarsByParticipant(userUuid, this.currentPage(), this.pageSize())
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (page) => {
          this.webinars.set(page.content ?? []);
          this.totalPages.set(page.totalPages ?? 0);
          this.totalElements.set(page.totalElements ?? 0);
        },
        error: (error) => {
          console.error('Failed to load participant webinars', error);
          this.loadError.set('Απέτυχε η φόρτωση των συμμετεχόντων στα σεμινάρια.');
        },
      });
  }

  /**
   * Loads the previous page of enrolled webinars.
   */
  previousPage(): void {
    if (this.currentPage() === 0) {
      return;
    }

    this.currentPage.update((page) => page - 1);
    this.loadMyWebinars();
  }

  /**
   * Loads the  page of enrolled webinars.
   */
  nextPage(): void {
    if (this.currentPage() >= this.totalPages() - 1) {
      return;
    }

    this.currentPage.update((page) => page + 1);
    this.loadMyWebinars();
  }

  /**
   * Unenrolls the current authenticated user from a webinar
   * and removes it from the visible list.
   *
   * @param webinarUuid webinar UUID
   */
  unenrollFromWebinar(webinarUuid: string): void {
    this.loadError.set(null);
    this.successMessage.set(null);

    const userUuid = this.authService.getCurrentUserUuid();

    if (!userUuid) {
      this.loadError.set('Δεν ήταν δυνατή η αναγνώριση του συνδεδεμένου χρήστη.');
      return;
    }

    this.webinarService.unenrollFromWebinar(webinarUuid, userUuid).subscribe({
      next: () => {
        this.successMessage.set('Η απεγγραφή από το σεμινάριο ολοκληρώθηκε με επιτυχία.');

        if (this.webinars().length === 1 && this.currentPage() > 0) {
          this.currentPage.update((page) => page - 1);
        }
        this.loadMyWebinars();

        setTimeout(() => {
          this.successMessage.set(null);
        }, 2000);
      },
      error: (error) => {
        console.error('Failed to unenroll from webinar', error);

        if (error.status === 400) {
          this.loadError.set('Δεν είστε εγγεγραμμένος σε αυτό το σεμινάριο.');
          return;
        }

        this.loadError.set('Η απεγγραφή από το σεμινάριο απέτυχε.');
      },
    });
  }
}