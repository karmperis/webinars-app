import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';

import { Navbar } from '../../layout/navbar/navbar';

import { User } from '../../../shared/services/user';
import { Role } from '../../../shared/services/role';

import { RoleReadOnly } from '../../../shared/interfaces/role-read-only';

/**
 * Component responsible for updating a user's role and active status.
 */
@Component({
  selector: 'app-edit-user-access',
  imports: [ReactiveFormsModule, RouterLink, Navbar],
  templateUrl: './edit-user-access.html',
})
export class EditUserAccess implements OnInit {
  private readonly userService = inject(User);
  private readonly roleService = inject(Role);
  private readonly route = inject(ActivatedRoute);

  private userUuid = '';

  readonly roles = signal<RoleReadOnly[]>([]);
  readonly isLoading = signal(true);
  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  /**
   * Reactive form based on the backend UserAdminEditDTO.
   */
  accessForm = new FormGroup({
    roleUuid: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    active: new FormControl(true, {
      nonNullable: true,
    }),
  });

  ngOnInit(): void {
    this.userUuid = this.route.snapshot.paramMap.get('uuid') ?? '';

    if (!this.userUuid) {
      this.errorMessage.set('Δεν βρέθηκε ο χρήστης.');
      this.isLoading.set(false);
      return;
    }

    this.loadUserAccessData();
    this.accessForm.valueChanges.subscribe(() => {
      this.errorMessage.set(null);
    });
  }

  /**
   * Loads the selected user and the available roles.
   */
  private loadUserAccessData(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      user: this.userService.getUserByUuid(this.userUuid),
      roles: this.roleService.getRoles(),
    })
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: ({ user, roles }) => {
          this.roles.set(roles);

          this.accessForm.patchValue({
            roleUuid: user.roleUuid,
            active: user.active,
          });

          this.accessForm.markAsPristine();
        },
        error: (error) => {
          console.error('Failed to load user access data', error);
          this.errorMessage.set('Απέτυχε η φόρτωση των στοιχείων πρόσβασης χρήστη.');
        },
      });
  }

  /**
   * Submits the form and updates the selected user's access.
   */
  onSubmit(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.accessForm.invalid) {
      this.accessForm.markAllAsTouched();
      return;
    }

    if (!this.accessForm.dirty) {
      return;
    }

    this.isSubmitting.set(true);

    this.userService
      .updateUserAccess(this.userUuid, this.accessForm.getRawValue())
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set('Η πρόσβαση χρήστη ενημερώθηκε επιτυχώς.');
          this.accessForm.markAsPristine();

          setTimeout(() => {
            this.successMessage.set(null);
          }, 2000);
        },
        error: (error) => {
          console.error('Failed to update user access', error);
          this.errorMessage.set('Η ενημέρωση πρόσβασης χρήστη απέτυχε.');
        },
      });
  }
}