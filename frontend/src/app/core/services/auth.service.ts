import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { EMPTY, Observable, finalize, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, RegisterRequest, AuthResponse, LoginResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly TOKEN_KEY = 'mycar_token';
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  /** Real email used for API calls during the 2FA verification flow. */
  pending2FAEmail: string | null = null;
  /** Masked email (a***@domain.com) used only for display in the verify screen. */
  pending2FAMaskedEmail: string | null = null;

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        if (response.requires2FA) {
          this.pending2FAEmail = credentials.email;          // real email for API calls
          this.pending2FAMaskedEmail = response.email ?? null; // masked for display only
        } else if (response.token) {
          this.saveToken(response.token);
        }
      }),
    );
  }

  verify2FA(email: string, code: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/verify-2fa`, { email, code }).pipe(
      tap((response) => {
        this.saveToken(response.token);
        this.pending2FAEmail = null;
        this.pending2FAMaskedEmail = null;
      }),
    );
  }

  resend2FA(email: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/verify-2fa/resend`, { email });
  }

  cancel2FA(email: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/verify-2fa/cancel`, { body: { email } });
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data).pipe(
      tap((response) => this.saveToken(response.token)),
    );
  }

  logout(): Observable<void> {
    const token = this.getToken();
    // Remove token immediately so the interceptor won't re-attach it to this very request.
    localStorage.removeItem(this.TOKEN_KEY);
    if (!token) {
      this.router.navigate(['/auth/login']);
      return EMPTY;
    }
    return this.http
      .post<void>(`${this.apiUrl}/logout`, {}, {
        headers: new HttpHeaders({ Authorization: `Bearer ${token}` }),
      })
      .pipe(finalize(() => this.router.navigate(['/auth/login'])));
  }

  clearLocalSession(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private saveToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }
}
