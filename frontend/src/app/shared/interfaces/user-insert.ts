/**
 * Payload used when creating a new user.
 */
export interface UserInsert {
  username: string;
  password: string;
  roleId: number;
  firstname: string;
  lastname: string;
  phoneNumber: string;
}