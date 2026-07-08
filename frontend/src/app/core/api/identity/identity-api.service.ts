import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
  fullName?: string;
  phone?: string;
}

export interface TokenResponse {
  accessToken: string;
  expiresInSeconds: number;
}

@Injectable({ providedIn: 'root' })
export class IdentityApiService {
  private readonly http = inject(HttpClient);

  register(request: RegisterRequest): Observable<ApiResponse<unknown>> {
    return this.http.post<ApiResponse<unknown>>('/api/auth/register', request);
  }

  login(email: string, password: string): Observable<ApiResponse<TokenResponse>> {
    return this.http.post<ApiResponse<TokenResponse>>('/api/auth/login', { email, password }, { withCredentials: true });
  }

  loginWithGoogle(idToken: string): Observable<ApiResponse<TokenResponse>> {
    return this.http.post<ApiResponse<TokenResponse>>('/api/auth/google', { idToken }, { withCredentials: true });
  }

  refresh(): Observable<ApiResponse<TokenResponse>> {
    return this.http.post<ApiResponse<TokenResponse>>('/api/auth/refresh', {}, { withCredentials: true });
  }

  logout(accessToken: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>('/api/auth/logout', {}, {
      headers: { Authorization: `Bearer ${accessToken}` },
      withCredentials: true,
    });
  }
}
