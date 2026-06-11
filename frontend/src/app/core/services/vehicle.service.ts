import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Vehicle {
  id: number;
  plate: string;
  brand: string;
  model: string;
  year: number;
  color: string | null;
  currentKm: number;
  createdAt: string;
}

export interface CreateVehicleRequest {
  plate: string;
  brand: string;
  model: string;
  year: number;
  color?: string | null;
  initialKm: number;
}

export interface UpdateVehicleRequest {
  plate?: string | null;
  brand?: string | null;
  model?: string | null;
  year?: number | null;
  color?: string | null;
}

@Injectable({ providedIn: 'root' })
export class VehicleService {
  private readonly apiUrl = `${environment.apiUrl}/vehicles`;

  constructor(private http: HttpClient) {}

  getVehicles(): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(this.apiUrl);
  }

  getVehicleById(id: number): Observable<Vehicle> {
    return this.http.get<Vehicle>(`${this.apiUrl}/${id}`);
  }

  createVehicle(data: CreateVehicleRequest): Observable<Vehicle> {
    return this.http.post<Vehicle>(this.apiUrl, data);
  }

  updateVehicle(id: number, data: UpdateVehicleRequest): Observable<Vehicle> {
    return this.http.put<Vehicle>(`${this.apiUrl}/${id}`, data);
  }

  deleteVehicle(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
