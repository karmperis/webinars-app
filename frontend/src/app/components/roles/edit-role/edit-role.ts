import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
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
  private readonly router = inject(Router);

  private roleUuid = '';

  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
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

    if (this.roleForm.invalid) {
      this.roleForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    this.roleService
      .updateRole(this.roleUuid, this.roleForm.getRawValue())
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.router.navigate(['/roles']);
        },
        error: (error) => {
          console.error('Failed to update role', error);
          this.errorMessage.set('Η ενημέρωση του ρόλου απέτυχε.');
        },
      });
  }
}
