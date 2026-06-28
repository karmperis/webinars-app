import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';

/**
 * Prevents unauthenticated users or users with expired tokens, or users without proper capabilities from accessing protected routes.
 */
export const authGuard: CanActivateFn = (route) => {
  const auth = inject(Auth);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    auth.logout();
    router.navigate(['/login']);
    return false;
  }

  const requiredCapabilities = route.data?.['capabilities'] as string[] | undefined;

  if (!requiredCapabilities || requiredCapabilities.length === 0) {
    return true;
  }

  if (auth.hasAnyCapability(requiredCapabilities)) {
    return true;
  }

  router.navigate(['/webinars']);
  return false;
};