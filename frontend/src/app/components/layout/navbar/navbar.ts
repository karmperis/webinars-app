import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Auth } from '../../../shared/services/auth';

import { User } from '../../../shared/services/user';
import { UserReadOnly } from '../../../shared/interfaces/user-read-only';

/**
 * Top navigation bar displayed on protected pages.
 * Provides quick navigation actions and current user information along with logout functionality.
 */
@Component({
  selector: 'app-navbar',
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar implements OnInit {
  private readonly auth = inject(Auth);
  private readonly userService = inject(User);
  private readonly router = inject(Router);

  readonly currentUser = signal<UserReadOnly | null>(null);
  readonly canViewWebinars = computed(() => this.auth.hasCapability('VIEW_WEBINARS'));
  readonly canEnrollInWebinar = computed(() => this.auth.hasCapability('ENROLL_IN_WEBINAR'));
  readonly canCreateWebinar = computed(() => this.auth.hasCapability('CREATE_WEBINAR'));
  readonly canManageUsers = computed(() => this.auth.hasCapability('MANAGE_USERS'));
  readonly canManageRoles = computed(() => this.auth.hasCapability('MANAGE_ROLES'));
  readonly canManageCapabilities = computed(() => this.auth.hasCapability('MANAGE_CAPABILITIES'));
  readonly canViewReports = computed(() => this.auth.hasCapability('VIEW_REPORTS'));

  ngOnInit(): void {
    this.loadCurrentUser();
  }

  /**
   * Loads the currently authenticated user's profile information.
   */
  private loadCurrentUser(): void {
    const uuid = this.auth.getCurrentUserUuid();

    if (!uuid) {
      return;
    }

    this.userService.getUserByUuid(uuid).subscribe({
      next: (user) => {
        this.currentUser.set(user);
      },
      error: () => {
        this.currentUser.set(null);
      },
    });
  }

  /**
   * Logs out the current user by removing the stored JWT token
   * and redirecting to the login page.
   */
  logout(): void {
    this.auth.logout();
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }
}