/**
 * Represents a single row returned by a webinar report.
 */
export interface WebinarReportView {
  webinarTitle?: string;
  organizerUsername?: string;
  organizerFirstName?: string;
  organizerLastName?: string;
  totalParticipants?: number;
  totalWebinars?: number;
  totalDuration?: number;
  webinarStatus?: string;
  userStatus?: string;
}