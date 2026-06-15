import { HttpInterceptorFn } from '@angular/common/http';
import { TOKEN_KEY } from '../constants/auth.constants';

/**
 * Adds the JWT bearer token to outgoing HTTP requests when available.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (!token) {
    return next(req);
  }

  const authRequest = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(authRequest);
};