export type AlertType     = 'DATE' | 'KM';
export type UrgencyLevel  = 'INFORMATIVA' | 'ADVERTENCIA' | 'URGENTE';

export interface AlertResponse {
  id: number;
  vehicleId: number;
  title: string;
  alertType: AlertType;
  alertDate: string | null;
  alertKm: number | null;
  advanceDays: number;
  urgencyLevel: UrgencyLevel;
  active: boolean;
  createdAt: string;
}

export interface CreateAlertRequest {
  title: string;
  alertType: AlertType;
  alertDate?: string;
  alertKm?: number;
  advanceDays?: number;
}

export interface UpdateAlertRequest {
  title?: string;
  alertDate?: string;
  alertKm?: number;
  advanceDays?: number;
  active?: boolean;
}
