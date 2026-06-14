import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { Navbar } from '../../layout/navbar/navbar';
import { Auth } from '../../../shared/services/auth';
import { User } from '../../../shared/services/user';

/**
 * Component responsible for editing the current user's profile information.
 */
@Component({
  selector: 'app-edit-profile',
  imports: [ReactiveFormsModule, Navbar],
  templateUrl: './edit-profile.html',
})
export class EditProfile implements OnInit {
  private readonly authService = inject(Auth);
  private readonly userService = inject(User);

  private userUuid = '';

  readonly isLoading = signal(true);
  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly warningMessage = signal<string | null>(null);

  /**
   * Reactive form based on the backend UserEditDTO.
   */
  profileForm = new FormGroup({
    firstname: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(100)],
    }),
    lastname: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(100)],
    }),
    phoneNumber: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(20), Validators.pattern(/^\+?[0-9]{7,15}$/)],
    }),
  });

  ngOnInit(): void {
    const currentUserUuid = this.authService.getCurrentUserUuid();

    if (!currentUserUuid) {
      this.errorMessage.set('Δεν ήταν δυνατή η αναγνώριση του συνδεδεμένου χρήστη.');
      this.isLoading.set(false);
      return;
    }

    this.userUuid = currentUserUuid;
    this.loadProfile();
  }

  /**
   * Loads the current user's profile data.
   */
  private loadProfile(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.userService
      .getUserByUuid(this.userUuid)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (user) => {
          this.profileForm.patchValue({
            firstname: user.firstname,
            lastname: user.lastname,
            phoneNumber: user.phoneNumber ?? '',
          });
        },
        error: (error) => {
          console.error('Failed to load user profile', error);
          this.errorMessage.set('Απέτυχε η φόρτωση του προφίλ χρήστη.');
        },
      });
  }

  /**
   * Submits the form and updates the current user's profile.
   */
  onSubmit(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.warningMessage.set(null);

    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    if (!this.profileForm.dirty) {
      this.warningMessage.set('Δεν υπάρχουν αλλαγές για αποθήκευση.');
      return;
    }

    this.isSubmitting.set(true);

    this.userService
      .updateUser(this.userUuid, this.profileForm.getRawValue())
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set('Το προφίλ ενημερώθηκε επιτυχώς.');
          this.profileForm.markAsPristine();

          setTimeout(() => {
            this.successMessage.set(null);
          }, 1500);
        },
        error: (error) => {
          console.error('Failed to update user profile', error);
          this.errorMessage.set('Η ενημέρωση του προφίλ απέτυχε.');
        },
      });
  }
}