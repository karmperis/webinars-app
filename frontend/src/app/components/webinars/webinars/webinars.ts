import { Component, inject, OnInit, signal } from '@angular/core';
import { Navbar } from '../../layout/navbar/navbar';
import { WebinarReadOnly } from '../../../shared/interfaces/webinar-read-only';

import { Webinar } from '../../../shared/services/webinar';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';

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
  readonly webinars = signal<WebinarReadOnly[]>([]);
  readonly isLoading = signal(true);
  readonly loadError = signal<string | null>(null);

  ngOnInit(): void {
    this.loadWebinars();
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
          this.loadError.set('Απέτυχε η φόρτωση των webinars.');
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
        this.loadError.set('Η διαγραφή του σεμινάριου απέτυχε.');
      },
    });
  }
}