import { Injectable } from '@angular/core';
import { TokenResponse } from '../api/identity/identity-api.service';

const ACCESS_TOKEN_KEY = 'watch.accessToken';
const REFRESH_TOKEN_KEY = 'watch.refreshToken';
const ACCESS_TOKEN_EXPIRES_AT_KEY = 'watch.accessTokenExpiresAt';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  getAccessToken(): string | null {
    return sessionStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return sessionStorage.getItem(REFRESH_TOKEN_KEY);
  }

  save(tokens: TokenResponse): void {
    sessionStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
    sessionStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
    sessionStorage.setItem(ACCESS_TOKEN_EXPIRES_AT_KEY, String(Date.now() + tokens.expiresInSeconds * 1000));
  }

  clear(): void {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    sessionStorage.removeItem(ACCESS_TOKEN_EXPIRES_AT_KEY);
  }
}
