export type DocumentType =
  | 'CEDULA_VERDE'
  | 'CEDULA_AZUL'
  | 'ITV'
  | 'SEGURO'
  | 'LICENCIA'
  | 'PATENTE'
  | 'OTRO';

export type DocumentStatus = 'VIGENTE' | 'POR_VENCER' | 'VENCIDO' | 'SIN_FECHA';

export interface DocumentResponse {
  id: number;
  vehicleId: number;
  type: DocumentType;
  referenceNumber: string | null;
  issueDate: string | null;
  expiryDate: string | null;
  notes: string | null;
  status: DocumentStatus;
  hasFile: boolean;
  originalFileName: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface CreateDocumentRequest {
  type: DocumentType;
  referenceNumber?: string | null;
  issueDate?: string | null;
  expiryDate?: string | null;
  notes?: string | null;
}

export interface CreateDocumentResponse {
  document: DocumentResponse;
  warning: string | null;
}

export interface DocumentSummaryResponse {
  total: number;
  vigente: number;
  porVencer: number;
  vencido: number;
  sinFecha: number;
  compliancePercentage: number;
  nextExpiring: {
    type: DocumentType;
    label: string;
    expiryDate: string;
    daysLeft: number;
  } | null;
}
