import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { Navbar } from '../../layout/navbar/navbar';

import { Role } from '../../../shared/services/role';
import { CapabilityReadOnly } from '../../../shared/interfaces/capability-read-only';

/**
 * Component responsible for displaying and removing  the capabilities assigned to a role.
 */
@Component({
  selector: 'app-role-capabilities',
  imports: [Navbar, RouterLink],
  templateUrl: './role-capabilities.html',
})
export class RoleCapabilities implements OnInit {
  private readonly roleService = inject(Role);
  private readonly route = inject(ActivatedRoute);

  private roleUuid = '';

  readonly capabilities = signal<CapabilityReadOnly[]>([]);
  readonly isLoading = signal(true);
  readonly isRemoving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.roleUuid = this.route.snapshot.paramMap.get('uuid') ?? '';

    if (!this.roleUuid) {
      this.errorMessage.set('Δεν βρέθηκε ο ρόλος.');
      this.isLoading.set(false);
      return;
    }

    this.loadRoleCapabilities();
  }

  /**
   * Loads the capabilities assigned to the selected role.
   */
  private loadRoleCapabilities(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.roleService
      .getRoleCapabilities(this.roleUuid)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (capabilities) => {
          this.capabilities.set(capabilities);
        },
        error: (error) => {
          console.error('Failed to load role capabilities', error);
          this.errorMessage.set('Απέτυχε η φόρτωση των δικαιωμάτων του ρόλου.');
        },
      });
  }

  /**
   * Removes a capability from the selected role and refreshes the assigned capability list.
   *
   * @param capabilityUuid capability UUID to remove from the role
   */
  onRemoveCapability(capabilityUuid: string): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.isRemoving.set(true);

    this.roleService
      .removeCapabilityFromRole(this.roleUuid, capabilityUuid)
      .pipe(finalize(() => this.isRemoving.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set('Το δικαίωμα αφαιρέθηκε επιτυχώς από τον ρόλο.');

          setTimeout(() => {
            this.successMessage.set(null);
          }, 2000);

          this.loadRoleCapabilities();
        },
        error: (error) => {
          console.error('Failed to remove capability from role', error);
          this.errorMessage.set('Απέτυχε η αφαίρεση του δικαιώματος από τον ρόλο.');
        },
      });
  }
}
