import { MaintenanceLogResponse } from './maintenance.model';

export type TransferStatus = 'PENDING' | 'EXPIRED' | 'COMPLETED';

export interface TransferGenerateResponse {
  token: string;
  expiresAt: string;
  transferUrl: string;
  qrCodeBase64: string;
}

export interface TransferHistoryItemResponse {
  tokenId: number;
  vehicleId: number;
  vehiclePlate: string;
  vehicleBrand: string;
  vehicleModel: string;
  buyerName: string | null;
  status: TransferStatus;
  createdAt: string;
  expiresAt: string;
  completedAt: string | null;
}

export interface TransferPreviewResponse {
  vehiclePlate: string;
  vehicleBrand: string;
  vehicleModel: string;
  vehicleYear: number;
  vehicleColor: string | null;
  currentKm: number;
  sellerName: string;
  expiresAt: string;
  maintenanceHistory: MaintenanceLogResponse[];
}

export interface TransferConfirmResponse {
  vehicleId: number;
  vehiclePlate: string;
  message: string;
}
