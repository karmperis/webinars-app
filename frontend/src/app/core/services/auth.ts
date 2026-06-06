import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthenticationRequest } from '../../shared/interfaces/authentication-request';
import { AuthenticationResponse } from '../../shared/interfaces/authentication-response';

const TOKEN_KEY = 'jwtToken';

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
   * Stores the JWT token in browser local storage.
   *
   * @param token JWT token returned by the backend
   */
  saveToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  /**
   * Retrieves the JWT token from browser local storage.
   *
   * @returns stored JWT token or null if the user is not authenticated
   */
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  /**
   * Removes the JWT token from browser local storage.
   */
  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  /**
   * Checks whether a JWT token exists in browser local storage.
   *
   * @returns true if the user has a stored token
   */
  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }
}