import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { Navbar } from '../../layout/navbar/navbar';

import { Capability } from '../../../shared/services/capability';

/**
 * Component responsible for editing an existing capability.
 */
@Component({
  selector: 'app-edit-capability',
  imports: [ReactiveFormsModule, RouterLink, Navbar],
  templateUrl: './edit-capability.html',
})
export class EditCapability implements OnInit {
  private readonly capabilityService = inject(Capability);
  private readonly route = inject(ActivatedRoute);
  
  private capabilityUuid = '';

  readonly isLoading = signal(true);
  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  /**
   * Reactive form based on the backend CapabilityEditDTO.
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
    this.capabilityUuid = this.route.snapshot.paramMap.get('uuid') ?? '';

    if (!this.capabilityUuid) {
      this.errorMessage.set('Δεν βρέθηκε το δικαίωμα.');
      this.isLoading.set(false);
      return;
    }

    this.loadCapability();

    this.capabilityForm.valueChanges.subscribe(() => {
  this.errorMessage.set(null);
});
  }

  /**
   * Loads the selected capability and fills the edit form.
   */
  private loadCapability(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.capabilityService
      .getCapabilityByUuid(this.capabilityUuid)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (capability) => {
          this.capabilityForm.patchValue({
            name: capability.name,
            description: capability.description,
          });

          this.capabilityForm.markAsPristine();

        },
        error: (error) => {
          console.error('Failed to load capability', error);
          this.errorMessage.set('Απέτυχε η φόρτωση του δικαιώματος.');
        },
      });
  }

  /**
   * Submits the form and updates the selected capability.
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
      .updateCapability(this.capabilityUuid, this.capabilityForm.getRawValue())
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
         this.successMessage.set('Το δικαίωμα ενημερώθηκε επιτυχώς.');
        
         this.capabilityForm.markAsPristine();

        setTimeout(() => {
          this.successMessage.set(null);
        }, 2000);
        },
        error: (error) => {
          console.error('Failed to update capability', error);

          if (error.status === 409) {
          this.errorMessage.set('Υπάρχει ήδη δικαίωμα με αυτό το όνομα.');
          return;
        }
        
          this.errorMessage.set('Η ενημέρωση του δικαιώματος απέτυχε.');
        },
      });
  }
}