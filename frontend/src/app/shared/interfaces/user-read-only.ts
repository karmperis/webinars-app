/**
 * Represents a user returned by the backend API.
 */
export interface UserReadOnly {
  uuid: string;
  username: string;
  active: boolean;
  roleId: number;
  roleName: string;
  firstname: string;
  lastname: string;
  phoneNumber: string;
}