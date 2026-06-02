/**
 * Payload used when creating a new webinar.
 */
export interface WebinarInsert {
  title: string;
  description: string;
  scheduledDate: string;
  duration: number;
}