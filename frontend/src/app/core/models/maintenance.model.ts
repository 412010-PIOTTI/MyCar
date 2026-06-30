export type MaintenanceSystem =
  | 'MOTOR'
  | 'TRANSMISION'
  | 'FRENOS'
  | 'ELECTRICO'
  | 'SUSPENSION'
  | 'CARROCERIA'
  | 'OTRO';

export interface MaintenanceLogResponse {
  id: number;
  vehicleId: number;
  system: MaintenanceSystem;
  date: string;
  kmAtMaintenance: number;
  workshop: string | null;
  description: string | null;
  cost: number | null;
  nextServiceKm: number | null;
  nextServiceDate: string | null;
  expenseId: number | null;
  createdAt: string;
}

export interface SystemHealthEntry {
  name: string;
  healthPct: number | null;
  recommendation: string | null;
}

export interface MaintenanceHealthResponse {
  overallStatus: 'OPTIMO' | 'REGULAR' | 'CRITICO';
  systems: SystemHealthEntry[];
}

export interface MaintenanceAnnualCostResponse {
  year: number;
  total: number;
  previousYearTotal: number;
  changePercent: number | null;
}

export interface CreateMaintenanceLogRequest {
  system: MaintenanceSystem;
  date: string;
  kmAtMaintenance: number;
  workshop: string;
  description?: string | null;
  cost?: number | null;
  nextServiceKm?: number | null;
  nextServiceDate?: string | null;
  expenseId?: number | null;
}
