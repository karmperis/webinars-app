import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { Navbar } from '../../layout/navbar/navbar';

import { Capability } from '../../../shared/services/capability';
import { CapabilityReadOnly } from '../../../shared/interfaces/capability-read-only';

/**
 * Component responsible for displaying all available capabilities.
 */
@Component({
  selector: 'app-capabilities',
  imports: [Navbar, RouterLink],
  templateUrl: './capabilities.html',
})
export class Capabilities implements OnInit {
  private readonly capabilityService = inject(Capability);

  readonly capabilities = signal<CapabilityReadOnly[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly actionErrorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadCapabilities();
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
   * Displays an action error message for a short period.
   *
   * @param message error message to display
   */
  private showActionError(message: string): void {
    this.actionErrorMessage.set(message);

    setTimeout(() => {
      this.actionErrorMessage.set(null);
    }, 2000);
  }

  /**
   * Loads all capabilities from the backend API.
   */
  private loadCapabilities(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.capabilityService
      .getCapabilities()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (capabilities) => {
          this.capabilities.set(capabilities);
        },
        error: (error) => {
          console.error('Failed to load capabilities', error);
          this.errorMessage.set('Απέτυχε η φόρτωση των δικαιωμάτων.');
        },
      });
  }

  /**
   * Deletes a capability and refreshes the displayed list.
   *
   * @param uuid capability UUID
   */
  deleteCapability(uuid: string): void {
    if (!confirm('Είστε σίγουροι ότι θέλετε να διαγράψετε αυτό το δικαίωμα;')) {
      return;
    }

    this.actionErrorMessage.set(null);
    this.successMessage.set(null);

    this.capabilityService.deleteCapability(uuid).subscribe({
      next: () => {
        this.showSuccess('Το δικαίωμα διαγράφηκε επιτυχώς.');
        this.loadCapabilities();
      },
      error: (error) => {
        console.error('Failed to delete capability', error);
        this.errorMessage.set('Η διαγραφή του δικαιώματος απέτυχε.');
      },
    });
  }
}