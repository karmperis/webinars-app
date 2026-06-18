import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { Navbar } from '../../layout/navbar/navbar';

import { Role } from '../../../shared/services/role';

/**
 * Component responsible for editing an existing role.
 */
@Component({
  selector: 'app-edit-role',
  imports: [ReactiveFormsModule, RouterLink, Navbar],
  templateUrl: './edit-role.html',
})
export class EditRole implements OnInit {
  private readonly roleService = inject(Role);
  private readonly route = inject(ActivatedRoute);

  private roleUuid = '';

  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly isSubmitting = signal(false);

  /**
   * Reactive form based on the backend RoleEditDTO.
   */
  roleForm = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(4), Validators.maxLength(50)],
    }),
  });

  ngOnInit(): void {
    this.roleUuid = this.route.snapshot.paramMap.get('uuid') ?? '';

    if (!this.roleUuid) {
      this.errorMessage.set('Δεν βρέθηκε ο ρόλος.');
      this.isLoading.set(false);
      return;
    }

    this.loadRole();
    this.roleForm.valueChanges.subscribe(() => {
      this.errorMessage.set(null);
      this.successMessage.set(null);
    });
  }

  /**
   * Loads the selected role and fills the edit form.
   */
  private loadRole(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.roleService
      .getRoleByUuid(this.roleUuid)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (role) => {
          this.roleForm.patchValue({
            name: role.name,
          });
          this.roleForm.markAsPristine();
        },
        error: (error) => {
          console.error('Failed to load role', error);
          this.errorMessage.set('Απέτυχε η φόρτωση του ρόλου.');
        },
      });
  }

  /**
   * Submits the form and updates the selected role.
   */
  onSubmit(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.roleForm.invalid) {
      this.roleForm.markAllAsTouched();
      return;
    }

    if (!this.roleForm.dirty) {
      return;
    }

    this.isSubmitting.set(true);

    this.roleService
      .updateRole(this.roleUuid, this.roleForm.getRawValue())
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set('Ο ρόλος ενημερώθηκε επιτυχώς.');
          this.roleForm.markAsPristine();

          setTimeout(() => {
            this.successMessage.set(null);
          }, 2000);
        },
        error: (error) => {
          console.error('Failed to update role', error);

          if (error.status === 409) {
            this.errorMessage.set('Υπάρχει ήδη ρόλος με αυτό το όνομα.');
            return;
          }
          
          this.errorMessage.set('Η ενημέρωση του ρόλου απέτυχε.');
        },
      });
  }
}