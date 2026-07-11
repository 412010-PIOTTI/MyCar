import { AlertResponse } from './alert.model';
import { DocumentResponse } from './document.model';
import { MaintenanceLogResponse } from './maintenance.model';

export interface VehicleDashboardSummary {
  vehicleId: number;
  plate: string;
  brand: string;
  model: string;
  currentKm: number;
  activeAlerts: AlertResponse[];
  documents: DocumentResponse[];
  lastService: MaintenanceLogResponse | null;
}

export interface ExchangeRateResponse {
  baseCurrency: string;
  targetCurrency: string;
  rate: number;
  date: string;
}

export interface DashboardResponse {
  vehicles: VehicleDashboardSummary[];
  totalExpensesMonth: number;
  totalExpensesYear: number;
  exchangeRate: ExchangeRateResponse | null;
}
