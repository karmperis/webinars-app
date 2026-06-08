import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Auth } from '../../../shared/services/auth';

/**
 * Top navigation bar displayed on protected pages.
 * Provides quick navigation actions and user logout functionality.
 */
@Component({
  selector: 'app-navbar',
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  /**
   * Logs out the current user by removing the stored JWT token
   * and redirecting to the login page.
   */
  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}