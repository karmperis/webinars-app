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

  ngOnInit(): void {
    this.loadRoles();
  }

  /**
   * Loads all roles from the backend API.
   */
  private loadRoles(): void {
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

    this.roleService.deleteRole(uuid).subscribe({
      next: () => {
        this.loadRoles();
      },
      error: (error) => {
        console.error('Failed to delete role', error);
        this.errorMessage.set('Η διαγραφή του ρόλου απέτυχε.');
      },
    });
  }
}