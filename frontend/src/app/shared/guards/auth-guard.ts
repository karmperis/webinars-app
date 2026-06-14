import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';

/**
 * Prevents unauthenticated users or users with expired tokens, or users without proper roles from accessing protected routes.
 */
export const authGuard: CanActivateFn = (route) => {
  const auth = inject(Auth);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    auth.logout();
    router.navigate(['/login']);
    return false;
  }

  const allowedRoles = route.data?.['roles'] as string[] | undefined;

  if (!allowedRoles || allowedRoles.length === 0) {
    return true;
  }

  const currentRole = auth.getCurrentUserRole();

  if (currentRole && allowedRoles.includes(currentRole)) {
    return true;
  }

  router.navigate(['/webinars']);
  return false;
};