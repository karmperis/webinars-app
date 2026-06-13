/**
 * Payload used by an administrator to update user access rights.
 */
export interface UserAdminEdit {
  roleUuid: string;
  active: boolean;
}