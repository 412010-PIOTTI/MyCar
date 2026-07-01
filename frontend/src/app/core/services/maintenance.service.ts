import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateMaintenanceLogRequest,
  MaintenanceAnnualCostResponse,
  MaintenanceHealthResponse,
  MaintenanceLogResponse,
} from '../models/maintenance.model';

@Injectable({ providedIn: 'root' })
export class MaintenanceService {
  private http = inject(HttpClient);

  private base(vehicleId: number): string {
    return `${environment.apiUrl}/vehicles/${vehicleId}/maintenance`;
  }

  getMaintenanceLogs(vehicleId: number): Observable<MaintenanceLogResponse[]> {
    return this.http.get<MaintenanceLogResponse[]>(this.base(vehicleId));
  }

  getMaintenanceLogById(vehicleId: number, logId: number): Observable<MaintenanceLogResponse> {
    return this.http.get<MaintenanceLogResponse>(`${this.base(vehicleId)}/${logId}`);
  }

  createMaintenanceLog(vehicleId: number, data: CreateMaintenanceLogRequest): Observable<MaintenanceLogResponse> {
    return this.http.post<MaintenanceLogResponse>(this.base(vehicleId), data);
  }

  getHealth(vehicleId: number): Observable<MaintenanceHealthResponse> {
    return this.http.get<MaintenanceHealthResponse>(`${this.base(vehicleId)}/health`);
  }

  getAnnualCost(vehicleId: number, year?: number): Observable<MaintenanceAnnualCostResponse> {
    const url = `${this.base(vehicleId)}/annual-cost${year ? `?year=${year}` : ''}`;
    return this.http.get<MaintenanceAnnualCostResponse>(url);
  }
}
