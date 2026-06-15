import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { Navbar } from '../../layout/navbar/navbar';

import { Capability } from '../../../shared/services/capability';

/**
 * Component responsible for creating new capabilities.
 */
@Component({
  selector: 'app-create-capability',
  imports: [ReactiveFormsModule, RouterLink, Navbar],
  templateUrl: './create-capability.html',
})
export class CreateCapability {
  private readonly capabilityService = inject(Capability);
  private readonly router = inject(Router);

  readonly errorMessage = signal<string | null>(null);
  readonly isSubmitting = signal(false);

  /**
   * Reactive form based on the backend CapabilityInsertDTO.
   */
  capabilityForm = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(4), Validators.maxLength(50)],
    }),
    description: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(255)],
    }),
  });

  /**
   * Submits the form and creates a new capability.
   */
  onSubmit(): void {
    this.errorMessage.set(null);

    if (this.capabilityForm.invalid) {
      this.capabilityForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    this.capabilityService
      .createCapability(this.capabilityForm.getRawValue())
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.router.navigate(['/capabilities']);
        },
        error: (error) => {
          console.error('Failed to create capability', error);
          this.errorMessage.set('Η δημιουργία του δικαιώματος απέτυχε.');
        },
      });
  }
}