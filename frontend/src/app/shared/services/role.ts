import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { RoleReadOnly } from '../interfaces/role-read-only';
import { RoleInsert } from '../interfaces/role-insert';
import { RoleEdit } from '../interfaces/role-edit';
import { CapabilityReadOnly } from '../interfaces/capability-read-only';

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

  /**
   * Creates a new role.
   *
   * @param role payload containing role creation data
   * @returns observable containing the created role
   */
  createRole(role: RoleInsert): Observable<RoleReadOnly> {
    return this.http.post<RoleReadOnly>(this.rolesUrl, role);
  }

  /**
   * Updates an existing role.
   *
   * @param uuid role UUID
   * @param role payload containing updated role data
   * @returns observable containing the updated role
   */
  updateRole(uuid: string, role: RoleEdit): Observable<RoleReadOnly> {
    return this.http.put<RoleReadOnly>(`${this.rolesUrl}/${uuid}`, role);
  }

  /**
   * Soft-deletes a role by UUID.
   *
   * @param uuid role UUID
   * @returns observable completed when deletion succeeds
   */
  deleteRole(uuid: string): Observable<void> {
    return this.http.delete<void>(`${this.rolesUrl}/${uuid}`);
  }

  /**
   * Assigns a capability to a role.
   *
   * @param roleUuid role UUID
   * @param capabilityUuid capability UUID
   * @returns completion observable
   */
  assignCapabilityToRole(roleUuid: string, capabilityUuid: string): Observable<void> {
    return this.http.post<void>(`${this.rolesUrl}/${roleUuid}/capabilities/${capabilityUuid}`, {});
  }
  /**
   * Retrieves all capabilities assigned to a role.
   *
   * @param roleUuid role UUID
   * @returns observable containing the assigned capabilities
   */
  getRoleCapabilities(roleUuid: string): Observable<CapabilityReadOnly[]> {
    return this.http.get<CapabilityReadOnly[]>(`${this.rolesUrl}/${roleUuid}/capabilities/view`);
  }
}