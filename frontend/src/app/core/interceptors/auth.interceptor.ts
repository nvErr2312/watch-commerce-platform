import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenStorageService } from '../auth/token-storage.service';

const PUBLIC_AUTH_ENDPOINTS = ['/auth/login', '/auth/google', '/auth/register', '/auth/refresh', '/auth/logout'];

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(TokenStorageService).getAccessToken();
  const isPublicAuthRequest = PUBLIC_AUTH_ENDPOINTS.some((endpoint) => request.url.includes(endpoint));

  if (!token || isPublicAuthRequest) {
    return next(request);
  }

  return next(request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  }));
};
