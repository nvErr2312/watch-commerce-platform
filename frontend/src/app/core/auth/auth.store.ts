import { computed, inject, Injectable, signal } from '@angular/core';
import { catchError, map, Observable, of, tap } from 'rxjs';
import { IdentityApiService, RegisterRequest, TokenResponse } from '../api/identity/identity-api.service';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly identityApi = inject(IdentityApiService);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly accessToken = signal(this.tokenStorage.getAccessToken());

  readonly isAuthenticated = computed(() => Boolean(this.accessToken()));

  readonly currentUser = computed(() => {
    const token = this.accessToken();
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      // Decode base64url safely
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      const parsed = JSON.parse(jsonPayload) as { sub: string; email: string; role: string };
      return {
        id: Number(parsed.sub),
        email: parsed.email,
        role: parsed.role,
      };
    } catch (e) {
      console.error('Error decoding auth token:', e);
      return null;
    }
  });


  login(email: string, password: string): Observable<TokenResponse> {
    return this.identityApi.login(email, password).pipe(
      map((response) => response.data),
      tap((tokens) => this.saveTokens(tokens)),
    );
  }

  loginWithGoogle(idToken: string): Observable<TokenResponse> {
    return this.identityApi.loginWithGoogle(idToken).pipe(
      map((response) => response.data),
      tap((tokens) => this.saveTokens(tokens)),
    );
  }

  register(request: RegisterRequest): Observable<unknown> {
    return this.identityApi.register(request).pipe(map((response) => response.data));
  }

  logout(): Observable<void> {
    const accessToken = this.tokenStorage.getAccessToken();

    if (!accessToken) {
      this.logoutLocal();
      return of(void 0);
    }

    return this.identityApi.logout(accessToken).pipe(
      catchError(() => of(null)),
      tap(() => this.logoutLocal()),
      map(() => void 0),
    );
  }

  logoutLocal(): void {
    this.tokenStorage.clear();
    this.accessToken.set(null);
  }

  private saveTokens(tokens: TokenResponse): void {
    this.tokenStorage.save(tokens);
    this.accessToken.set(tokens.accessToken);
  }
}
