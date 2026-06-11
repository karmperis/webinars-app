import { Component, inject, OnInit, signal } from '@angular/core';

import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';

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
  imports: [Navbar, DatePipe],
  templateUrl: './organizer-webinars.html',
  styleUrl: './organizer-webinars.css',
})
export class OrganizerWebinars implements OnInit {
  private readonly webinarService = inject(Webinar);
  private readonly authService = inject(Auth);

  readonly webinars = signal<WebinarReadOnly[]>([]);
  readonly isLoading = signal(true);
  readonly loadError = signal<string | null>(null);

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
      .getWebinarsByOrganizer(userUuid)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (page) => {
          this.webinars.set(page.content ?? []);
        },
        error: (error) => {
          console.error('Failed to load organizer webinars', error);
          this.loadError.set('Απέτυχε η φόρτωση των σεμιναρίων διοργάνωσης.');
        },
      });
  }
}