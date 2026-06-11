import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { UserInsert } from '../interfaces/user-insert';
import { UserReadOnly } from '../interfaces/user-read-only';

import { PageResponse } from '../interfaces/page-response';
import { UserEdit } from '../interfaces/user-edit';
import { UserAdminEdit } from '../interfaces/user-admin-edit';

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

  /**
   * Retrieves all users from the backend API.
   *
   * @returns observable containing a page of users
   */
  getUsers(): Observable<PageResponse<UserReadOnly>> {
    return this.http.get<PageResponse<UserReadOnly>>(this.usersUrl);
  }

  /**
   * Retrieves a user by UUID.
   *
   * @param uuid user UUID
   * @returns observable containing the requested user
   */
  getUserByUuid(uuid: string): Observable<UserReadOnly> {
    return this.http.get<UserReadOnly>(`${this.usersUrl}/${uuid}`);
  }

  /**
   * Updates a user's profile information.
   *
   * @param uuid user UUID
   * @param user payload containing editable profile data
   * @returns observable containing the updated user
   */
  updateUser(uuid: string, user: UserEdit): Observable<UserReadOnly> {
    return this.http.put<UserReadOnly>(`${this.usersUrl}/${uuid}`, user);
  }

  /**
   * Updates a user's access settings.
   *
   * @param uuid user UUID
   * @param user payload containing role and active status
   * @returns observable containing the updated user
   */
  updateUserAccess(uuid: string, user: UserAdminEdit): Observable<UserReadOnly> {
    return this.http.patch<UserReadOnly>(`${this.usersUrl}/${uuid}/access`, user);
  }

  /**
   * Soft-deletes a user by UUID.
   *
   * @param uuid user UUID
   * @returns observable completed when deletion succeeds
   */
  deleteUser(uuid: string): Observable<void> {
    return this.http.delete<void>(`${this.usersUrl}/${uuid}`);
  }
}