/**
 * Payload used when a webinar is updated.
 */
export interface WebinarEdit {
  title: string;
  description: string;
  scheduledDate: string;
  duration: number;
}