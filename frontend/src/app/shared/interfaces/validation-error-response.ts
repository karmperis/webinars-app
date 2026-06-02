/**
 * Interface representing a validation error response from the server.
 */
export interface ValidationErrorResponse {
  code: string;
  message: string;
  errors: Record<string, string>;
}