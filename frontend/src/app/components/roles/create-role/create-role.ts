import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
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
export class CreateRole implements OnInit {
  private readonly roleService = inject(Role);

  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
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

  ngOnInit(): void {
    this.roleForm.valueChanges.subscribe(() => {
      this.errorMessage.set(null);
    });
  }

  /**
   * Submits the form and creates a new role.
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
      .createRole(this.roleForm.getRawValue())
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set('Ο ρόλος δημιουργήθηκε επιτυχώς.');

          this.roleForm.reset({
            name: '',
          });

          setTimeout(() => {
            this.successMessage.set(null);
          }, 2000);
        },
        error: (error) => {
          console.error('Failed to create role', error);

          if (error.status === 409) {
            this.errorMessage.set('Υπάρχει ήδη ρόλος με αυτό το όνομα.');
            return;
          }
          
          this.errorMessage.set('Η δημιουργία του ρόλου απέτυχε.');
        },
      });
  }
}