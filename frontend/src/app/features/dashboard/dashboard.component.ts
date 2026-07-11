import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardResponse, VehicleDashboardSummary } from '../../core/models/dashboard.model';
import { DocumentStatus, DocumentType } from '../../core/models/document.model';
import { SectionCardComponent } from '../../shared/components/section-card/section-card.component';
import { StatBadgeComponent } from '../../shared/components/stat-badge/stat-badge.component';
import { UrgencyBadgeComponent } from '../../shared/components/urgency-badge/urgency-badge.component';

interface QuickLink {
  label: string;
  route: string;
  icon: string;
}

const DOCUMENT_TYPE_LABELS: Record<DocumentType, string> = {
  CEDULA_VERDE: 'Cédula Verde',
  CEDULA_AZUL: 'Cédula Azul',
  ITV: 'ITV',
  SEGURO: 'Póliza de Seguro',
  LICENCIA: 'Licencia de Conducir',
  PATENTE: 'Patente',
  OTRO: 'Otro documento',
};

const DOCUMENT_STATUS_BADGE: Record<DocumentStatus, string> = {
  VIGENTE: 'bg-emerald-100 text-emerald-700',
  POR_VENCER: 'bg-amber-100 text-amber-700',
  VENCIDO: 'bg-red-100 text-red-700',
  SIN_FECHA: 'bg-gray-100 text-gray-500',
};

const DOCUMENT_STATUS_LABEL: Record<DocumentStatus, string> = {
  VIGENTE: 'Vigente',
  POR_VENCER: 'Por vencer',
  VENCIDO: 'Vencido',
  SIN_FECHA: 'Sin fecha',
};

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, SectionCardComponent, StatBadgeComponent, UrgencyBadgeComponent],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private dashboardService = inject(DashboardService);

  dashboard: DashboardResponse | null = null;
  loading = true;
  loadError = false;

  readonly quickLinks: QuickLink[] = [
    { label: 'Mis vehículos', route: '/vehicles', icon: '🚗' },
    { label: 'Mantenimiento', route: '/maintenance', icon: '🔧' },
    { label: 'Gastos', route: '/expenses', icon: '💳' },
    { label: 'Documentos', route: '/documents', icon: '📎' },
    { label: 'Alertas', route: '/alerts', icon: '🔔' },
    { label: 'Transferencias', route: '/transfers', icon: '➡️' },
  ];

  ngOnInit(): void {
    this.dashboardService
      .getDashboard()
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.loading = false)))
      .subscribe({
        next: (dashboard) => { this.dashboard = dashboard; },
        error: () => { this.loadError = true; },
      });
  }

  vehicleLabel(vehicle: VehicleDashboardSummary): string {
    return `${vehicle.brand} ${vehicle.model} · ${vehicle.plate}`;
  }

  documentTypeLabel(type: DocumentType): string {
    return DOCUMENT_TYPE_LABELS[type] ?? type;
  }

  documentStatusBadgeClass(status: DocumentStatus): string {
    return DOCUMENT_STATUS_BADGE[status] ?? 'bg-gray-100 text-gray-500';
  }

  documentStatusLabel(status: DocumentStatus): string {
    return DOCUMENT_STATUS_LABEL[status] ?? status;
  }

  formatKm(km: number): string {
    return km.toLocaleString('es-AR') + ' km';
  }

  formatDate(dateStr: string | null): string {
    if (!dateStr) return '—';
    const [y, m, d] = dateStr.split('T')[0].split('-');
    const months = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    return `${d} ${months[+m - 1]}, ${y}`;
  }

  trackByVehicleId(_: number, vehicle: VehicleDashboardSummary): number { return vehicle.vehicleId; }
}
