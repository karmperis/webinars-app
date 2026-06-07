import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { UserInsert } from '../interfaces/user-insert';
import { UserReadOnly } from '../interfaces/user-read-only';

/**
 * Service responsible for user-related API operations.
 */
@Injectable({
  providedIn: 'root',
})
export class User {
  private readonly http = inject(HttpClient);
  private readonly usersUrl = `${environment.apiUrl}/users`;

  /**
   * Creates a new user account.
   *
   * @param user payload containing the new user's registration data
   * @returns observable containing the created user
   */
  createUser(user: UserInsert): Observable<UserReadOnly> {
    return this.http.post<UserReadOnly>(this.usersUrl, user);
  }
}