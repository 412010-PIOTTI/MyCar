import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { VehicleService } from '../../../core/services/vehicle.service';
import { MaintenanceService } from '../../../core/services/maintenance.service';
import { Vehicle } from '../../../core/models/vehicle.model';
import {
  MaintenanceLogResponse,
  MaintenanceSystem,
} from '../../../core/models/maintenance.model';
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

  // ── Vehicle selector ──────────────────────────────────────────────────────
  vehicles: Vehicle[]             = [];
  selectedVehicle: Vehicle | null = null;
  vehiclesLoading                 = true;

  // ── Data ──────────────────────────────────────────────────────────────────
  allRecords: MaintenanceLogResponse[] = [];

  loading   = false;
  loadError = false;

  // ── Client-side filter ────────────────────────────────────────────────────
  activeSystem: MaintenanceSystem | null = null;

  get records(): MaintenanceLogResponse[] {
    if (!this.activeSystem) return this.allRecords;
    return this.allRecords.filter((r) => r.system === this.activeSystem);
  }

  get totalCost(): number | null {
    const logs = this.allRecords.filter((r) => r.cost != null);
    if (logs.length === 0) return null;
    return logs.reduce((sum, r) => sum + (r.cost ?? 0), 0);
  }

  get lastKm(): number | null {
    if (this.allRecords.length === 0) return null;
    return this.allRecords.reduce((max, r) => Math.max(max, r.kmAtMaintenance), 0);
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
  detailLog: MaintenanceLogResponse | null = null;

  ngOnInit(): void {
    this.vehicleService
      .getVehicles()
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.vehiclesLoading = false)))
      .subscribe({
        next: (vehicles) => {
          this.vehicles = vehicles;
          if (vehicles.length > 0) this.selectedVehicle = vehicles[0];
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

    this.maintenanceSvc.getMaintenanceLogs(vehicleId)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.loading = false)))
      .subscribe({
        next: (logs) => { this.allRecords = logs; },
        error: () => (this.loadError = true),
      });
  }

  onLogCreated(): void {
    this.showRegisterModal = false;
    this.loadAll();
  }

  viewLog(log: MaintenanceLogResponse): void { this.detailLog = log; }
  closeDetail(): void { this.detailLog = null; }

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

}
