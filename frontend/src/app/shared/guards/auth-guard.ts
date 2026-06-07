import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

const TOKEN_KEY = 'jwtToken';

/**
 * Prevents unauthenticated users from accessing protected routes.
 */
export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = localStorage.getItem(TOKEN_KEY);

  if (token) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};