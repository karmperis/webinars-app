import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthenticationRequest } from '../interfaces/authentication-request';
import { AuthenticationResponse } from '../interfaces/authentication-response';
import { TOKEN_KEY } from '../constants/auth.constants';

/**
 * Service responsible for user authentication and JWT token management.
 */
@Injectable({
  providedIn: 'root',
})
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly authUrl = `${environment.apiUrl}/auth`;

  /**
   * Authenticates a user and returns a JWT token response.
   *
   * @param credentials username and password submitted by the user
   * @returns observable containing the authentication token
   */
  login(credentials: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.authUrl}/authenticate`, credentials);
  }

  /**
   * Stores the JWT token either persistently or for the current browser session.
   *
   * @param token JWT token returned by the backend
   * @param rememberMe whether the token should persist after closing the browser
   */
  saveToken(token: string, rememberMe: boolean = false): void {
    const storage = rememberMe ? localStorage : sessionStorage;
    storage.setItem(TOKEN_KEY, token);
  }

  /**
   * Retrieves the JWT token from browser storage.
   *
   * @returns stored JWT token or null if the user is not authenticated
   */
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY) ?? sessionStorage.getItem(TOKEN_KEY);
  }

  /**
   * Removes the JWT token from browser storage.
   */
  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(TOKEN_KEY);
  }

  /**
   * Checks whether the stored JWT token is valid and not expired.
   *
   * @returns true if the user has a valid non-expired token
   */
  isAuthenticated(): boolean {
    return this.isTokenValid();
  }

  /**
   * Checks whether the stored JWT token is valid and not expired.
   *
   * @returns true if the JWT token exists, can be decoded and has not expired
   */
  isTokenValid(): boolean {
    const payload = this.getTokenPayload();

    if (!payload || !payload['exp']) {
      return false;
    }

    const currentTimeInSeconds = Math.floor(Date.now() / 1000);

    return payload['exp'] > currentTimeInSeconds;
  }
  /**
   * Returns the decoded JWT payload.
   *
   * @returns decoded JWT payload or null if token is invalid
   */
  private getTokenPayload(): Record<string, any> | null {
    const token = this.getToken();

    if (!token) {
      return null;
    }

    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch {
      return null;
    }
  }

  /**
   * Returns the UUID of the currently authenticated user.
   *
   * @returns user UUID or null if unavailable
   */
  getCurrentUserUuid(): string | null {
    const payload = this.getTokenPayload();

    return payload?.['uuid'] ?? null;
  }

  /**
   * Returns the role of the currently authenticated user.
   *
   * @returns user role or null if unavailable
   */
  getCurrentUserRole(): string | null {
    const payload = this.getTokenPayload();

    return payload?.['role'] ?? null;
  }

  /**
   * Checks whether the current user has the given role.
   *
   * @param role role name to check
   * @returns true if the current user has the given role
   */
  hasRole(role: string): boolean {
    return this.getCurrentUserRole() === role;
  }

  /**
   * Returns the capabilities of the currently authenticated user.
   *
   * @returns capability names from the JWT token
   */
  getCurrentUserCapabilities(): string[] {
    const payload = this.getTokenPayload();

    return payload?.['capabilities'] ?? [];
  }

  /**
   * Checks whether the current user has the given capability.
   *
   * @param capability capability name to check
   * @returns true if the current user has the given capability
   */
  hasCapability(capability: string): boolean {
    return this.getCurrentUserCapabilities().includes(capability);
  }

  /**
   * Checks whether the current user has at least one of the given capabilities.
   *
   * @param capabilities capability names to check
   * @returns true if the current user has at least one required capability
   */
  hasAnyCapability(capabilities: string[]): boolean {
    return capabilities.some((capability) => this.hasCapability(capability));
  }
}