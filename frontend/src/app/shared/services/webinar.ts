import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PageResponse } from '../interfaces/page-response';
import { WebinarReadOnly } from '../interfaces/webinar-read-only';
import { WebinarInsert } from '../interfaces/webinar-insert';
import { WebinarEdit } from '../interfaces/webinar-edit';

/**
 * Service responsible for webinar-related API operations.
 */
@Injectable({
  providedIn: 'root',
})
export class Webinar {
  private readonly http = inject(HttpClient);
  private readonly webinarsUrl = `${environment.apiUrl}/webinars`;

  /**
   * Retrieves all webinars from the backend API.
   *
   * @returns observable containing the list of webinars
   */
  getWebinars(): Observable<PageResponse<WebinarReadOnly>> {
    return this.http.get<PageResponse<WebinarReadOnly>>(this.webinarsUrl);
  }

  /**
   * Creates a new webinar.
   *
   * @param webinar payload containing the webinar creation data
   * @returns observable containing the created webinar
   */
  createWebinar(webinar: WebinarInsert): Observable<WebinarReadOnly> {
    return this.http.post<WebinarReadOnly>(this.webinarsUrl, webinar);
  }

  /**
   * Deletes a webinar by its UUID.
   *
   * @param uuid webinar UUID
   * @returns observable completed when deletion succeeds
   */
  deleteWebinar(uuid: string): Observable<void> {
    return this.http.delete<void>(`${this.webinarsUrl}/${uuid}`);
  }

  /**
   * Retrieves a webinar by its UUID.
   *
   * @param uuid webinar UUID
   * @returns observable containing the requested webinar
   */
  getWebinarByUuid(uuid: string): Observable<WebinarReadOnly> {
    return this.http.get<WebinarReadOnly>(`${this.webinarsUrl}/${uuid}`);
  }

  /**
   * Updates an existing webinar.
   *
   * @param uuid webinar UUID
   * @param webinar payload containing updated webinar data
   * @returns observable containing the updated webinar
   */
  updateWebinar(uuid: string, webinar: WebinarEdit): Observable<WebinarReadOnly> {
    return this.http.put<WebinarReadOnly>(`${this.webinarsUrl}/${uuid}`, webinar);
  }

  /**
   * Enrolls a user in a webinar.
   *
   * @param webinarUuid the webinar UUID
   * @param userUuid the user UUID
   * @returns completion observable
   */
  enrollInWebinar(webinarUuid: string, userUuid: string): Observable<void> {
    return this.http.post<void>(`${this.webinarsUrl}/${webinarUuid}/participants/${userUuid}`, {});
  }

  /**
   * Retrieves webinars where a user is enrolled as participant.
   *
   * @param userUuid the participant user UUID
   * @returns observable containing the user's enrolled webinars
   */
  getWebinarsByParticipant(userUuid: string): Observable<PageResponse<WebinarReadOnly>> {
    return this.http.get<PageResponse<WebinarReadOnly>>(
      `${this.webinarsUrl}/participants/${userUuid}`,
    );
  }
}