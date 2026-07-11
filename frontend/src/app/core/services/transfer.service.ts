import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  TransferConfirmResponse,
  TransferGenerateResponse,
  TransferHistoryItemResponse,
  TransferPreviewResponse,
} from '../models/transfer.model';

@Injectable({ providedIn: 'root' })
export class TransferService {
  private http = inject(HttpClient);

  private base(vehicleId: number): string {
    return `${environment.apiUrl}/vehicles/${vehicleId}/transfer`;
  }

  generate(vehicleId: number): Observable<TransferGenerateResponse> {
    return this.http.post<TransferGenerateResponse>(`${this.base(vehicleId)}/generate`, {});
  }

  getActiveToken(vehicleId: number): Observable<TransferGenerateResponse | null> {
    return this.http.get<TransferGenerateResponse | null>(`${this.base(vehicleId)}/active`);
  }

  getHistory(): Observable<TransferHistoryItemResponse[]> {
    return this.http.get<TransferHistoryItemResponse[]>(`${environment.apiUrl}/vehicles/transfer/history`);
  }

  preview(token: string): Observable<TransferPreviewResponse> {
    return this.http.get<TransferPreviewResponse>(`${environment.apiUrl}/vehicles/transfer/${token}/preview`);
  }

  confirm(token: string): Observable<TransferConfirmResponse> {
    return this.http.post<TransferConfirmResponse>(`${environment.apiUrl}/vehicles/transfer/${token}/confirm`, {});
  }
}
