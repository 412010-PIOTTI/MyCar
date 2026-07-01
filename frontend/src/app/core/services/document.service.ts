import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateDocumentRequest,
  CreateDocumentResponse,
  DocumentResponse,
  DocumentSummaryResponse,
} from '../models/document.model';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private http = inject(HttpClient);

  private base(vehicleId: number): string {
    return `${environment.apiUrl}/vehicles/${vehicleId}/documents`;
  }

  getDocuments(vehicleId: number): Observable<DocumentResponse[]> {
    return this.http.get<DocumentResponse[]>(this.base(vehicleId));
  }

  createDocument(vehicleId: number, data: CreateDocumentRequest): Observable<CreateDocumentResponse> {
    return this.http.post<CreateDocumentResponse>(this.base(vehicleId), data);
  }

  deleteDocument(vehicleId: number, documentId: number): Observable<void> {
    return this.http.delete<void>(`${this.base(vehicleId)}/${documentId}`);
  }

  getSummary(vehicleId: number): Observable<DocumentSummaryResponse> {
    return this.http.get<DocumentSummaryResponse>(`${this.base(vehicleId)}/summary`);
  }

  uploadFile(vehicleId: number, documentId: number, file: File): Observable<DocumentResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<DocumentResponse>(`${this.base(vehicleId)}/${documentId}/file`, formData);
  }

  downloadFile(vehicleId: number, documentId: number): Observable<Blob> {
    return this.http.get(`${this.base(vehicleId)}/${documentId}/file`, { responseType: 'blob' });
  }
}
