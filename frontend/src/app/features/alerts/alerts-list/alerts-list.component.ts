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
  // Pre-computed at load time — never recalculated during change detection
  icon:        string;
  statusBadge: string;
  metaLine:    string;
  remaining:   string;
  progress:    number;   // 0–100
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

  vehicles: Vehicle[]        = [];
  allAlerts: EnrichedAlert[] = [];
  loading   = true;
  loadError = false;

  get urgentAlerts():  EnrichedAlert[] { return this.allAlerts.filter(a => a.alert.urgencyLevel === 'URGENTE'); }
  get warningAlerts(): EnrichedAlert[] { return this.allAlerts.filter(a => a.alert.urgencyLevel === 'ADVERTENCIA'); }
  get infoAlerts():    EnrichedAlert[] { return this.allAlerts.filter(a => a.alert.urgencyLevel === 'INFORMATIVA'); }

  ngOnInit(): void { this.load(); }

  reload(): void { this.load(); }

  padCount(n: number): string { return n.toString().padStart(2, '0'); }

  trackById(_: number, e: EnrichedAlert): number { return e.alert.id; }

  // ── Private ───────────────────────────────────────────────────────────────

  private load(): void {
    this.loading   = true;
    this.loadError = false;
    this.allAlerts = [];

    this.vehicleService.getVehicles()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        switchMap(vehicles => {
          this.vehicles = vehicles;
          if (vehicles.length === 0) return of([] as EnrichedAlert[]);
          return forkJoin(
            vehicles.map(v =>
              this.alertService.getAlerts(v.id).pipe(
                map(alerts => alerts.map(a => this.enrich(a, v)))
              )
            )
          ).pipe(map(results => results.flat()));
        }),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next:  (alerts) => { this.allAlerts = alerts; },
        error: ()       => { this.loadError = true; },
      });
  }

  /** Compute all display values once, using a fixed timestamp to avoid NG0100. */
  private enrich(alert: AlertResponse, vehicle: Vehicle): EnrichedAlert {
    const now = Date.now();  // single snapshot for this alert

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
    if (alert.alertType === 'KM' && alert.alertKm != null) {
      return vehicle.currentKm >= alert.alertKm ? 'Km Excedido' : 'Próximo';
    }
    if (alert.alertType === 'DATE' && alert.alertDate) {
      return now >= new Date(alert.alertDate).getTime() ? 'Vencido' : 'Vence Pronto';
    }
    return '';
  }

  private computeMetaLine(alert: AlertResponse, vehicle: Vehicle): string {
    if (alert.alertType === 'DATE' && alert.alertDate) {
      return `📅 Vence: ${this.formatDate(alert.alertDate)}`;
    }
    if (alert.alertType === 'KM' && alert.alertKm != null) {
      return `⊙ KM actual: ${this.formatKm(vehicle.currentKm)}    ⏱ Límite: ${this.formatKm(alert.alertKm)}`;
    }
    return '';
  }

  private computeRemaining(alert: AlertResponse, vehicle: Vehicle, now: number): string {
    if (alert.alertType === 'KM' && alert.alertKm != null) {
      const remaining = alert.alertKm - vehicle.currentKm;
      if (remaining <= 0) return 'Límite superado';
      return `A ${remaining.toLocaleString('es-AR')} km del límite`;
    }
    if (alert.alertType === 'DATE' && alert.alertDate) {
      const diffDays = Math.ceil((new Date(alert.alertDate).getTime() - now) / 86_400_000);
      if (diffDays <= 0) return 'Vencido';
      return `Faltan ${diffDays} día${diffDays !== 1 ? 's' : ''}`;
    }
    return '';
  }

  private computeProgress(alert: AlertResponse, vehicle: Vehicle, now: number): number {
    if (alert.alertType === 'KM' && alert.alertKm != null) {
      return Math.min(100, (vehicle.currentKm / alert.alertKm) * 100);
    }
    if (alert.alertType === 'DATE' && alert.alertDate) {
      const alertDate = new Date(alert.alertDate).getTime();
      const startDate = alertDate - Math.max(alert.advanceDays, 1) * 86_400_000;
      const total     = alertDate - startDate;
      return Math.min(100, Math.max(0, ((now - startDate) / total) * 100));
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
