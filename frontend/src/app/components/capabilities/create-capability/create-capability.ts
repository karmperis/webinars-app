import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
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
export class CreateCapability implements OnInit {
  private readonly capabilityService = inject(Capability);

  readonly errorMessage = signal<string | null>(null);
  readonly isSubmitting = signal(false);
  readonly successMessage = signal<string | null>(null);

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

  ngOnInit(): void {
    this.capabilityForm.valueChanges.subscribe(() => {
      this.errorMessage.set(null);
    });
  }

  /**
   * Submits the form and creates a new capability.
   */
  onSubmit(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.capabilityForm.invalid) {
      this.capabilityForm.markAllAsTouched();
      return;
    }
    if (!this.capabilityForm.dirty) {
      return;
    }

    this.isSubmitting.set(true);

    this.capabilityService
      .createCapability(this.capabilityForm.getRawValue())
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set('Το δικαίωμα δημιουργήθηκε επιτυχώς.');

          this.capabilityForm.reset({
            name: '',
            description: '',
          });

          setTimeout(() => {
            this.successMessage.set(null);
          }, 2000);
        },
        error: (error) => {
          console.error('Failed to create capability', error);
          
          if (error.status === 409) {
            this.errorMessage.set('Υπάρχει ήδη δικαίωμα με αυτό το όνομα.');
            return;
          }
          
          this.errorMessage.set('Η δημιουργία του δικαιώματος απέτυχε.');
        },
      });
  }
}