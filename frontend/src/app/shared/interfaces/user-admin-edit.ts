/**
 * Payload used by an administrator to update user access rights.
 */
export interface UserAdminEdit {
  roleId: number;
  active: boolean;
}