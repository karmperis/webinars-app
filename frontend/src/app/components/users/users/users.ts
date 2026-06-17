import { Component, inject, OnInit, signal } from '@angular/core';

import { finalize } from 'rxjs';
import { RouterLink } from '@angular/router';
import { Navbar } from '../../layout/navbar/navbar';

import { User } from '../../../shared/services/user';
import { UserReadOnly } from '../../../shared/interfaces/user-read-only';
import { Router } from '@angular/router';

/**
 * Component responsible for displaying registered users.
 */
@Component({
  selector: 'app-users',
  imports: [Navbar, RouterLink],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class Users implements OnInit {
  private readonly userService = inject(User);
  private readonly router = inject(Router);

  readonly users = signal<UserReadOnly[]>([]);
  readonly isLoading = signal(true);
  readonly loadError = signal<string | null>(null);

  ngOnInit(): void {
    this.loadUsers();
  }

  /**
   * Loads users from the backend API.
   */
  private loadUsers(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.userService
      .getUsers()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (page) => {
          this.users.set(page.content ?? []);
        },
        error: (error) => {
          console.error('Failed to load users', error);
          this.loadError.set('Απέτυχε η φόρτωση των χρηστών.');
        },
      });
  }

  /**
   * Deletes a user and refreshes the displayed list.
   *
   * @param uuid user UUID
   */
  deleteUser(uuid: string): void {
    if (!confirm('Είστε σίγουροι ότι θέλετε να διαγράψετε αυτόν τον χρήστη;')) {
      return;
    }

    this.userService.deleteUser(uuid).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (error) => {
        console.error('Failed to delete user', error);
        this.loadError.set('Η διαγραφή του χρήστη απέτυχε.');
      },
    });
  }

  /**
   * Navigates to the user access edit page.
   *
   * @param uuid user UUID
   */
  editUserAccess(uuid: string): void {
    this.router.navigate(['/users', uuid, 'access']);
  }
}