import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserProfile, UpdateProfileRequest, ChangePasswordRequest } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  getMe(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/me`);
  }

  updateProfile(data: UpdateProfileRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/me`, data);
  }

  changePassword(data: ChangePasswordRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/me/password`, data);
  }

  toggle2FA(enabled: boolean, password: string): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/me/2fa`, { enabled, password });
  }

  deleteAccount(password: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/me`, { body: { password } });
  }
}
