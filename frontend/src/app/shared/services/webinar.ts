import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PageResponse } from '../interfaces/page-response';
import { WebinarReadOnly } from '../interfaces/webinar-read-only';

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
}