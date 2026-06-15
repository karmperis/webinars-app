import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { Navbar } from '../../layout/navbar/navbar';

import { Role } from '../../../shared/services/role';

/**
 * Component responsible for creating new roles.
 */
@Component({
  selector: 'app-create-role',
  imports: [ReactiveFormsModule, RouterLink, Navbar],
  templateUrl: './create-role.html',
})
export class CreateRole {
  private readonly roleService = inject(Role);
  private readonly router = inject(Router);

  readonly errorMessage = signal<string | null>(null);
  readonly isSubmitting = signal(false);

  /**
   * Reactive form based on the backend RoleInsertDTO.
   */
  roleForm = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(4), Validators.maxLength(50)],
    }),
  });

  /**
   * Submits the form and creates a new role.
   */
  onSubmit(): void {
    this.errorMessage.set(null);

    if (this.roleForm.invalid) {
      this.roleForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    this.roleService
      .createRole(this.roleForm.getRawValue())
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.router.navigate(['/roles']);
        },
        error: (error) => {
          console.error('Failed to create role', error);
          this.errorMessage.set('Η δημιουργία του ρόλου απέτυχε.');
        },
      });
  }
}