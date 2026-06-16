import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { Auth } from '../../../shared/services/auth';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})

/**
 * Login component responsible for authenticating users and storing JWT tokens.
 */
export class Login {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  readonly errorMessage = signal<string | null>(null);
  readonly showPassword = signal(false);

  /**
   * Reactive login form containing username and password fields.
   */
  loginForm = new FormGroup({
    username: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    rememberMe: new FormControl(false, {
      nonNullable: true,
    }),
  });

  /**
   * Submits the login form and stores the JWT token on success.
   */
  onSubmit(): void {
    this.errorMessage.set(null);
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const { rememberMe, ...credentials } = this.loginForm.getRawValue();

    this.auth.login(credentials).subscribe({
      next: (response) => {
        this.auth.saveToken(response.token, rememberMe);
        this.router.navigate(['/webinars']);
      },
      error: () => {
        this.errorMessage.set('Τα στοιχεία σύνδεσης δεν είναι σωστά.');
      },
    });
  }
  /**
   * Toggles password visibility in the login form.
   */
  togglePasswordVisibility(): void {
    this.showPassword.update((value) => !value);
  }
}