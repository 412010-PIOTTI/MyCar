import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin, of, switchMap, finalize, map } from 'rxjs';
import { VehicleService } from '../../../core/services/vehicle.service';
import { AlertService } from '../../../core/services/alert.service';
import { Vehicle } from '../../../core/models/vehicle.model';
import { AlertResponse } from '../../../core/models/alert.model';

interface EnrichedAlert {
  alert:       AlertResponse;
  vehicle:     Vehicle;
  icon:        string;
  statusBadge: string;
  metaLine:    string;
  remaining:   string;
  progress:    number;
}

@Component({
  selector: 'app-alerts-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alerts-list.component.html',
})
export class AlertsListComponent implements OnInit {
  private destroyRef     = inject(DestroyRef);
  private vehicleService = inject(VehicleService);
  private alertService   = inject(AlertService);

  vehicles: Vehicle[]             = [];
  selectedVehicle: Vehicle | null = null;
  vehiclesLoading = true;

  allAlerts: EnrichedAlert[] = [];
  loading   = false;
  loadError = false;

  get allVehiclesMode(): boolean { return this.selectedVehicle === null && this.vehicles.length > 0; }

  get urgentAlerts():  EnrichedAlert[] { return this.allAlerts.filter(a => a.alert.urgencyLevel === 'URGENTE'); }
  get warningAlerts(): EnrichedAlert[] { return this.allAlerts.filter(a => a.alert.urgencyLevel === 'ADVERTENCIA'); }
  get infoAlerts():    EnrichedAlert[] { return this.allAlerts.filter(a => a.alert.urgencyLevel === 'INFORMATIVA'); }

  ngOnInit(): void {
    this.vehicleService.getVehicles()
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.vehiclesLoading = false)))
      .subscribe({
        next: (vehicles) => {
          this.vehicles = vehicles;
          if (vehicles.length === 1) this.selectedVehicle = vehicles[0];
          if (vehicles.length > 0) this.load();
        },
        error: () => { this.loadError = true; },
      });
  }

  onVehicleChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedVehicle = value === '' ? null : (this.vehicles.find(v => v.id === Number(value)) ?? null);
    this.load();
  }

  reload(): void { this.load(); }

  padCount(n: number): string { return n.toString().padStart(2, '0'); }

  trackById(_: number, e: EnrichedAlert): number { return e.alert.id; }

  private load(): void {
    this.loading   = true;
    this.loadError = false;
    this.allAlerts = [];

    const source$ = this.selectedVehicle
      ? this.alertService.getAlerts(this.selectedVehicle.id).pipe(
          map(alerts => alerts.map(a => this.enrich(a, this.selectedVehicle!)))
        )
      : forkJoin(
          this.vehicles.map(v =>
            this.alertService.getAlerts(v.id).pipe(
              map(alerts => alerts.map(a => this.enrich(a, v)))
            )
          )
        ).pipe(map(results => results.flat()));

    source$
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.loading = false)))
      .subscribe({
        next:  alerts => { this.allAlerts = alerts; },
        error: ()     => { this.loadError = true; },
      });
  }

  private enrich(alert: AlertResponse, vehicle: Vehicle): EnrichedAlert {
    const now = Date.now();
    return {
      alert,
      vehicle,
      icon:        alert.alertType === 'KM' ? '🔧' : '📅',
      statusBadge: this.computeStatusBadge(alert, vehicle, now),
      metaLine:    this.computeMetaLine(alert, vehicle),
      remaining:   this.computeRemaining(alert, vehicle, now),
      progress:    this.computeProgress(alert, vehicle, now),
    };
  }

  private computeStatusBadge(alert: AlertResponse, vehicle: Vehicle, now: number): string {
    if (alert.alertType === 'KM' && alert.alertKm != null)
      return vehicle.currentKm >= alert.alertKm ? 'Km Excedido' : 'Próximo';
    if (alert.alertType === 'DATE' && alert.alertDate)
      return now >= new Date(alert.alertDate).getTime() ? 'Vencido' : 'Vence Pronto';
    return '';
  }

  private computeMetaLine(alert: AlertResponse, vehicle: Vehicle): string {
    if (alert.alertType === 'DATE' && alert.alertDate)
      return `📅 Vence: ${this.formatDate(alert.alertDate)}`;
    if (alert.alertType === 'KM' && alert.alertKm != null)
      return `⊙ KM actual: ${this.formatKm(vehicle.currentKm)}    ⏱ Límite: ${this.formatKm(alert.alertKm)}`;
    return '';
  }

  private computeRemaining(alert: AlertResponse, vehicle: Vehicle, now: number): string {
    if (alert.alertType === 'KM' && alert.alertKm != null) {
      const remaining = alert.alertKm - vehicle.currentKm;
      return remaining <= 0 ? 'Límite superado' : `A ${remaining.toLocaleString('es-AR')} km del límite`;
    }
    if (alert.alertType === 'DATE' && alert.alertDate) {
      const diffDays = Math.ceil((new Date(alert.alertDate).getTime() - now) / 86_400_000);
      return diffDays <= 0 ? 'Vencido' : `Faltan ${diffDays} día${diffDays !== 1 ? 's' : ''}`;
    }
    return '';
  }

  private computeProgress(alert: AlertResponse, vehicle: Vehicle, now: number): number {
    if (alert.alertType === 'KM' && alert.alertKm != null)
      return Math.min(100, (vehicle.currentKm / alert.alertKm) * 100);
    if (alert.alertType === 'DATE' && alert.alertDate) {
      const alertDate = new Date(alert.alertDate).getTime();
      const startDate = alertDate - Math.max(alert.advanceDays, 1) * 86_400_000;
      return Math.min(100, Math.max(0, ((now - startDate) / (alertDate - startDate)) * 100));
    }
    return 0;
  }

  private formatDate(dateStr: string): string {
    const [y, m, d] = dateStr.split('T')[0].split('-');
    const months = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];
    return `${d} ${months[+m - 1]} ${y}`;
  }

  private formatKm(km: number): string {
    return km.toLocaleString('es-AR') + ' km';
  }
}
