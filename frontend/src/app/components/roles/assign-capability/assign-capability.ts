import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { Navbar } from '../../layout/navbar/navbar';

import { Role } from '../../../shared/services/role';
import { Capability } from '../../../shared/services/capability';

import { CapabilityReadOnly } from '../../../shared/interfaces/capability-read-only';

/**
 * Component responsible for assigning capabilities to a selected role.
 */
@Component({
  selector: 'app-assign-capability',
  imports: [ReactiveFormsModule, RouterLink, Navbar],
  templateUrl: './assign-capability.html',
})
export class AssignCapability implements OnInit {
  private readonly roleService = inject(Role);
  private readonly capabilityService = inject(Capability);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private roleUuid = '';

  readonly capabilities = signal<CapabilityReadOnly[]>([]);
  readonly isLoading = signal(true);
  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  /**
   * Reactive form used to select the capability that will be assigned to the role.
   */
  assignForm = new FormGroup({
    capabilityUuid: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  ngOnInit(): void {
    this.roleUuid = this.route.snapshot.paramMap.get('uuid') ?? '';

    if (!this.roleUuid) {
      this.errorMessage.set('Δεν βρέθηκε ο ρόλος.');
      this.isLoading.set(false);
      return;
    }

    this.assignForm.valueChanges.subscribe(() => {
      this.errorMessage.set(null);
    });

    this.loadCapabilities();
  }

  /**
   * Loads all available capabilities from the backend API.
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
          this.assignForm.markAsPristine();
        },
        error: (error) => {
          console.error('Failed to load capabilities', error);
          this.errorMessage.set('Απέτυχε η φόρτωση των δικαιωμάτων.');
        },
      });
  }

  /**
   * Assigns the selected capability to the current role.
   */
  onSubmit(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.assignForm.invalid) {
      this.assignForm.markAllAsTouched();
      return;
    }

    if (!this.assignForm.dirty) {
      return;
    }

    const capabilityUuid = this.assignForm.controls.capabilityUuid.value;

    this.isSubmitting.set(true);

    this.roleService
      .assignCapabilityToRole(this.roleUuid, capabilityUuid)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set('Το δικαίωμα ανατέθηκε επιτυχώς στον ρόλο.');
          this.assignForm.reset({
            capabilityUuid: '',
          });

          setTimeout(() => {
            this.successMessage.set(null);
          }, 2000);
        },
        error: (error) => {
          console.error('Failed to assign capability to role', error);
          if (error.status === 409) {
            this.errorMessage.set('Το δικαίωμα έχει ήδη ανατεθεί στον συγκεκριμένο ρόλο.');
            return;
          }
          this.errorMessage.set('Η ανάθεση δικαιώματος στον ρόλο απέτυχε.');
        },
      });
  }
}