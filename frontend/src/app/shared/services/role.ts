import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { RoleReadOnly } from '../interfaces/role-read-only';

/**
 * Service responsible for role-related API operations.
 */
@Injectable({
  providedIn: 'root',
})
export class Role {
  private readonly http = inject(HttpClient);
  private readonly rolesUrl = `${environment.apiUrl}/roles`;

  /**
   * Retrieves all available roles.
   *
   * @returns observable containing the role list
   */
  getRoles(): Observable<RoleReadOnly[]> {
    return this.http.get<RoleReadOnly[]>(this.rolesUrl);
  }

  /**
   * Retrieves a role by UUID.
   *
   * @param uuid role UUID
   * @returns observable containing the role
   */
  getRoleByUuid(uuid: string): Observable<RoleReadOnly> {
    return this.http.get<RoleReadOnly>(`${this.rolesUrl}/${uuid}`);
  }
}