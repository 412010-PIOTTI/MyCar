import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, finalize, forkJoin, of } from 'rxjs';
import { VehicleService } from '../../../core/services/vehicle.service';
import { MaintenanceService } from '../../../core/services/maintenance.service';
import { AlertService } from '../../../core/services/alert.service';
import { Vehicle } from '../../../core/models/vehicle.model';
import {
  MaintenanceAnnualCostResponse,
  MaintenanceHealthResponse,
  MaintenanceLogResponse,
  MaintenanceSystem,
} from '../../../core/models/maintenance.model';
import { AlertResponse } from '../../../core/models/alert.model';
import { MaintenanceRegisterModalComponent } from '../maintenance-register-modal/maintenance-register-modal.component';

interface SystemTab {
  label: string;
  value: MaintenanceSystem | null;
}

interface SystemMeta {
  label: string;
  bgClass: string;
  textClass: string;
}

@Component({
  selector: 'app-maintenance-list',
  standalone: true,
  imports: [CommonModule, MaintenanceRegisterModalComponent],
  templateUrl: './maintenance-list.component.html',
})
export class MaintenanceListComponent implements OnInit {
  private destroyRef     = inject(DestroyRef);
  private vehicleService = inject(VehicleService);
  private maintenanceSvc = inject(MaintenanceService);
  private alertSvc       = inject(AlertService);

  // ── Vehicle selector ──────────────────────────────────────────────────────
  vehicles: Vehicle[]             = [];
  selectedVehicle: Vehicle | null = null;
  vehiclesLoading                 = true;

  // ── Data ──────────────────────────────────────────────────────────────────
  allRecords: MaintenanceLogResponse[]             = [];
  healthData: MaintenanceHealthResponse | null     = null;
  annualCost: MaintenanceAnnualCostResponse | null = null;
  urgentAlerts: AlertResponse[]                    = [];

  loading   = false;
  loadError = false;

  // ── Client-side filter ────────────────────────────────────────────────────
  activeSystem: MaintenanceSystem | null = null;

  get records(): MaintenanceLogResponse[] {
    if (!this.activeSystem) return this.allRecords;
    return this.allRecords.filter((r) => r.system === this.activeSystem);
  }

  readonly tabs: SystemTab[] = [
    { label: 'Todos',        value: null },
    { label: 'Motor',        value: 'MOTOR' },
    { label: 'Transmisión',  value: 'TRANSMISION' },
    { label: 'Frenos',       value: 'FRENOS' },
    { label: 'Eléctrico',    value: 'ELECTRICO' },
    { label: 'Suspensión',   value: 'SUSPENSION' },
    { label: 'Carrocería',   value: 'CARROCERIA' },
    { label: 'Otro',         value: 'OTRO' },
  ];

  readonly systemMeta: Record<string, SystemMeta> = {
    MOTOR:      { label: 'Motor',       bgClass: 'bg-emerald-100', textClass: 'text-emerald-800' },
    TRANSMISION:{ label: 'Transmisión', bgClass: 'bg-blue-100',    textClass: 'text-blue-800'    },
    FRENOS:     { label: 'Frenos',      bgClass: 'bg-red-100',     textClass: 'text-red-800'     },
    ELECTRICO:  { label: 'Eléctrico',   bgClass: 'bg-yellow-100',  textClass: 'text-yellow-800'  },
    SUSPENSION: { label: 'Suspensión',  bgClass: 'bg-amber-100',   textClass: 'text-amber-800'   },
    CARROCERIA: { label: 'Carrocería',  bgClass: 'bg-slate-100',   textClass: 'text-slate-700'   },
    OTRO:       { label: 'General',     bgClass: 'bg-indigo-100',  textClass: 'text-indigo-800'  },
  };

  // ── Modal ─────────────────────────────────────────────────────────────────
  showRegisterModal = false;

  ngOnInit(): void {
    this.vehicleService
      .getVehicles()
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.vehiclesLoading = false)))
      .subscribe({
        next: (vehicles) => {
          this.vehicles = vehicles;
          if (vehicles.length === 1) this.selectedVehicle = vehicles[0];
          if (vehicles.length > 0) this.loadAll();
        },
        error: () => { this.loadError = true; },
      });
  }

  selectVehicle(vehicle: Vehicle): void {
    this.selectedVehicle = vehicle;
    this.activeSystem    = null;
    this.loadAll();
  }

  onVehicleChange(event: Event): void {
    const id = Number((event.target as HTMLSelectElement).value);
    const v  = this.vehicles.find((v) => v.id === id);
    if (v) this.selectVehicle(v);
  }

  selectTab(system: MaintenanceSystem | null): void {
    this.activeSystem = system;
  }

  loadAll(): void {
    if (!this.selectedVehicle) return;
    this.loading   = true;
    this.loadError = false;
    const vehicleId = this.selectedVehicle.id;

    forkJoin({
      logs:       this.maintenanceSvc.getMaintenanceLogs(vehicleId),
      health:     this.maintenanceSvc.getHealth(vehicleId).pipe(catchError(() => of(null))),
      annualCost: this.maintenanceSvc.getAnnualCost(vehicleId).pipe(catchError(() => of(null))),
      alerts:     this.alertSvc.getAlerts(vehicleId).pipe(catchError(() => of([] as AlertResponse[]))),
    })
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.loading = false)))
      .subscribe({
        next: ({ logs, health, annualCost, alerts }) => {
          this.allRecords   = logs;
          this.healthData   = health;
          this.annualCost   = annualCost;
          this.urgentAlerts = alerts.filter(
            (a) => a.urgencyLevel === 'URGENTE' || a.urgencyLevel === 'ADVERTENCIA'
          );
        },
        error: () => (this.loadError = true),
      });
  }

  onLogCreated(): void {
    this.showRegisterModal = false;
    this.loadAll();
  }

  // ── Health helpers ────────────────────────────────────────────────────────
  getHealthBarColor(pct: number | null): string {
    if (pct == null) return 'bg-gray-200';
    if (pct >= 70)   return 'bg-emerald-500';
    if (pct >= 30)   return 'bg-amber-500';
    return 'bg-red-500';
  }

  getHealthPctClass(pct: number | null): string {
    if (pct == null) return 'text-text-muted';
    if (pct >= 70)   return 'text-emerald-700';
    if (pct >= 30)   return 'text-amber-700';
    return 'text-danger';
  }

  get overallStatusClass(): string {
    const s = this.healthData?.overallStatus;
    if (s === 'CRITICO') return 'bg-danger-light text-danger';
    if (s === 'REGULAR') return 'bg-warning-light text-warning-dark';
    return 'bg-success-light text-success-dark';
  }

  get overallStatusLabel(): string {
    const s = this.healthData?.overallStatus;
    if (s === 'CRITICO') return 'Estado General: Crítico';
    if (s === 'REGULAR') return 'Estado General: Regular';
    return 'Estado General: Óptimo';
  }

  // ── Alert helpers ─────────────────────────────────────────────────────────
  getUrgencyTextClass(level: string): string {
    if (level === 'URGENTE')     return 'text-danger font-semibold';
    if (level === 'ADVERTENCIA') return 'text-warning-dark font-semibold';
    return 'text-primary font-semibold';
  }

  alertSubtitle(alert: AlertResponse): string {
    if (alert.alertType === 'DATE' && alert.alertDate) {
      const d = this.formatDate(alert.alertDate);
      return alert.urgencyLevel === 'URGENTE' ? `Vencida el ${d}` : `Vence el ${d}`;
    }
    if (alert.alertType === 'KM' && alert.alertKm != null) {
      return alert.urgencyLevel === 'URGENTE' ? 'Kilometraje superado' : `A los ${this.formatKm(alert.alertKm)}`;
    }
    return 'Requiere atención';
  }

  // ── System badge helpers ──────────────────────────────────────────────────
  getLabel(system: string):     string { return this.systemMeta[system]?.label     ?? system; }
  getBgClass(system: string):   string { return this.systemMeta[system]?.bgClass   ?? 'bg-gray-100'; }
  getTextClass(system: string): string { return this.systemMeta[system]?.textClass ?? 'text-gray-800'; }

  trackById(_: number, log: MaintenanceLogResponse): number { return log.id; }

  // ── Format helpers ────────────────────────────────────────────────────────
  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    const [y, m, d] = dateStr.split('T')[0].split('-');
    const months = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];
    return `${d} ${months[+m - 1]}, ${y}`;
  }

  formatKm(km: number): string {
    return km.toLocaleString('es-AR') + ' km';
  }

  formatCost(cost: number | null): string {
    if (cost == null) return '—';
    return cost.toLocaleString('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 });
  }

  formatChangePercent(pct: number | null): string {
    if (pct == null) return '';
    const sign = pct > 0 ? '↑ +' : '↘ ';
    return `${sign}${Math.abs(pct).toFixed(1)}% vs año anterior`;
  }
}
