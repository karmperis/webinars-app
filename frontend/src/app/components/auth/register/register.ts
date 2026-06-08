import { Component, inject } from '@angular/core';

import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { User } from '../../../shared/services/user';

/**
 * Component responsible for user registration.
 */
@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private readonly userService = inject(User);
  private readonly router = inject(Router);

  errorMessage = '';
  successMessage = '';

  /**
   * Reactive registration form based on the backend UserInsertDTO.
   */
  registerForm = new FormGroup({
    username: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(4), Validators.maxLength(50)],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.pattern(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=]).{12,}$/),
      ],
    }),
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
      validators: [Validators.pattern(/^\+?[0-9]{7,15}$/)],
    }),
  });

  /**
   * Submits the registration form and creates a new user account.
   */
  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.userService.createUser(this.registerForm.getRawValue()).subscribe({
      next: () => {
        this.successMessage = 'Ο λογαριασμός δημιουργήθηκε επιτυχώς.';
        this.router.navigate(['/login']);
      },
      error: () => {
        this.errorMessage = 'Η εγγραφή απέτυχε. Ελέγξτε τα στοιχεία σας.';
      },
    });
  }
}