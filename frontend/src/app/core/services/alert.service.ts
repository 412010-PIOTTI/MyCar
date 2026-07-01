import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AlertResponse,
  CreateAlertRequest,
  UpdateAlertRequest,
} from '../models/alert.model';

@Injectable({ providedIn: 'root' })
export class AlertService {
  private http = inject(HttpClient);

  private base(vehicleId: number): string {
    return `${environment.apiUrl}/vehicles/${vehicleId}/alerts`;
  }

  getAlerts(vehicleId: number): Observable<AlertResponse[]> {
    return this.http.get<AlertResponse[]>(this.base(vehicleId));
  }

  getAlertById(vehicleId: number, alertId: number): Observable<AlertResponse> {
    return this.http.get<AlertResponse>(`${this.base(vehicleId)}/${alertId}`);
  }

  createAlert(vehicleId: number, data: CreateAlertRequest): Observable<AlertResponse> {
    return this.http.post<AlertResponse>(this.base(vehicleId), data);
  }

  updateAlert(vehicleId: number, alertId: number, data: UpdateAlertRequest): Observable<AlertResponse> {
    return this.http.patch<AlertResponse>(`${this.base(vehicleId)}/${alertId}`, data);
  }

  deleteAlert(vehicleId: number, alertId: number): Observable<void> {
    return this.http.delete<void>(`${this.base(vehicleId)}/${alertId}`);
  }
}
