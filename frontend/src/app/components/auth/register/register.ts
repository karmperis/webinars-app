import { Component, inject, OnInit, signal } from '@angular/core';

import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

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
export class Register implements OnInit {
  private readonly userService = inject(User);
  private readonly router = inject(Router);

  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly isSubmitting = signal(false);

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
    acceptTerms: new FormControl(false, {
      nonNullable: true,
      validators: [Validators.requiredTrue],
    }),
  });

  ngOnInit(): void {
    this.registerForm.valueChanges.subscribe(() => {
      this.errorMessage.set(null);
      this.successMessage.set(null);
    });
  }
  /**
   * Submits the registration form and creates a new user account.
   */
  onSubmit(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const { acceptTerms, ...formValue } = this.registerForm.getRawValue();

    const userInsert = {
      ...formValue,
      phoneNumber: formValue.phoneNumber.trim() || null,
    };

    this.isSubmitting.set(true);

    this.userService
      .createUser(userInsert)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set(
            'Ο λογαριασμός δημιουργήθηκε επιτυχώς. Θα μεταφερθείτε στη σελίδα σύνδεσης.',
          );

          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: (error) => {
          if (error.status === 409) {
            this.errorMessage.set('Το όνομα χρήστη χρησιμοποιείται ήδη.');
            return;
          }
          this.errorMessage.set('Η εγγραφή απέτυχε. Ελέγξτε τα στοιχεία σας.');
        },
      });
  }
}
