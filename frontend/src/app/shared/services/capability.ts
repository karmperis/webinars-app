import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CapabilityReadOnly } from '../interfaces/capability-read-only';
import { CapabilityInsert } from '../interfaces/capability-insert';
import { CapabilityEdit } from '../interfaces/capability-edit';

/**
 * Service responsible for capability-related API operations.
 */
@Injectable({
  providedIn: 'root',
})
export class Capability {
  private readonly http = inject(HttpClient);
  private readonly capabilitiesUrl = `${environment.apiUrl}/capabilities`;

  /**
   * Retrieves all capabilities from the backend API.
   */
  getCapabilities(): Observable<CapabilityReadOnly[]> {
    return this.http.get<CapabilityReadOnly[]>(this.capabilitiesUrl);
  }

  /**
   * Retrieves a capability by UUID.
   */
  getCapabilityByUuid(uuid: string): Observable<CapabilityReadOnly> {
    return this.http.get<CapabilityReadOnly>(`${this.capabilitiesUrl}/${uuid}`);
  }

  /**
   * Creates a new capability.
   */
  createCapability(capability: CapabilityInsert): Observable<CapabilityReadOnly> {
    return this.http.post<CapabilityReadOnly>(this.capabilitiesUrl, capability);
  }

  /**
   * Updates an existing capability.
   */
  updateCapability(uuid: string, capability: CapabilityEdit): Observable<CapabilityReadOnly> {
    return this.http.put<CapabilityReadOnly>(`${this.capabilitiesUrl}/${uuid}`, capability);
  }

  /**
   * Deletes a capability by UUID.
   */
  deleteCapability(uuid: string): Observable<void> {
    return this.http.delete<void>(`${this.capabilitiesUrl}/${uuid}`);
  }
}