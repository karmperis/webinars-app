import { Component, inject, OnInit, signal } from '@angular/core';

import { finalize } from 'rxjs';

import { RouterLink } from '@angular/router';

import { Navbar } from '../../layout/navbar/navbar';

import { Role } from '../../../shared/services/role';
import { RoleReadOnly } from '../../../shared/interfaces/role-read-only';

/**
 * Component responsible for displaying all available roles.
 */
@Component({
  selector: 'app-roles',
  imports: [Navbar, RouterLink],
  templateUrl: './roles.html',
})
export class Roles implements OnInit {
  private readonly roleService = inject(Role);

  readonly roles = signal<RoleReadOnly[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly actionErrorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.loadRoles();
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
   * Loads all roles from the backend API.
   */
  private loadRoles(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.roleService
      .getRoles()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (roles) => {
          this.roles.set(roles);
        },
        error: (error) => {
          console.error('Failed to load roles', error);
          this.errorMessage.set('Απέτυχε η φόρτωση των ρόλων.');
        },
      });
  }

  /**
   * Deletes a role and refreshes the displayed list.
   *
   * @param uuid role UUID
   */
  deleteRole(uuid: string): void {
    if (!confirm('Είστε σίγουροι ότι θέλετε να διαγράψετε αυτόν τον ρόλο;')) {
      return;
    }

    this.actionErrorMessage.set(null);
    this.successMessage.set(null);

    this.roleService.deleteRole(uuid).subscribe({
      next: () => {
        this.showSuccess('Ο ρόλος διαγράφηκε επιτυχώς.');
        this.loadRoles();
      },
      error: (error) => {
        console.error('Failed to delete role', error);
        this.showActionError(
          'Η διαγραφή του ρόλου απέτυχε. Ο ρόλος χρησιμοποιείται από έναν ή περισσότερους ενεργούς χρήστες.',
        );
      },
    });
  }
}