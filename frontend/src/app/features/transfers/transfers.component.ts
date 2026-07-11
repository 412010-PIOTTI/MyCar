import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { VehicleService } from '../../core/services/vehicle.service';
import { TransferService } from '../../core/services/transfer.service';
import { Vehicle } from '../../core/models/vehicle.model';
import {
  TransferGenerateResponse,
  TransferHistoryItemResponse,
  TransferStatus,
} from '../../core/models/transfer.model';

@Component({
  selector: 'app-transfers',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './transfers.component.html',
})
export class TransfersComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private vehicleService = inject(VehicleService);
  private transferService = inject(TransferService);

  // ── Vehicle selector ──────────────────────────────────────────────────────
  vehicles: Vehicle[] = [];
  selectedVehicle: Vehicle | null = null;
  vehiclesLoading = true;

  // ── Data ──────────────────────────────────────────────────────────────────
  activeToken: TransferGenerateResponse | null = null;
  history: TransferHistoryItemResponse[] = [];
  loading = false;
  loadError = false;

  // ── Generate ──────────────────────────────────────────────────────────────
  generating = false;
  generateError = '';

  ngOnInit(): void {
    this.loadHistory();

    this.vehicleService
      .getVehicles()
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.vehiclesLoading = false)))
      .subscribe({
        next: (vehicles) => {
          this.vehicles = vehicles;
          if (vehicles.length > 0) this.selectedVehicle = vehicles[0];
          if (this.selectedVehicle) this.loadActiveToken();
        },
        error: () => { this.loadError = true; },
      });
  }

  onVehicleChange(event: Event): void {
    const id = Number((event.target as HTMLSelectElement).value);
    const vehicle = this.vehicles.find((v) => v.id === id);
    if (!vehicle) return;
    this.selectedVehicle = vehicle;
    this.activeToken = null;
    this.loadActiveToken();
  }

  /** The active-token card is scoped to the selected vehicle (requires current ownership). */
  loadActiveToken(): void {
    if (!this.selectedVehicle) return;
    this.loading = true;
    this.loadError = false;
    this.transferService
      .getActiveToken(this.selectedVehicle.id)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.loading = false)))
      .subscribe({
        next: (token) => (this.activeToken = token),
        error: () => (this.loadError = true),
      });
  }

  /** The history table is global — it stays visible even for vehicles already transferred away. */
  loadHistory(): void {
    this.transferService
      .getHistory()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (history) => (this.history = history) });
  }

  generate(): void {
    if (!this.selectedVehicle) return;
    this.generating = true;
    this.generateError = '';
    this.transferService
      .generate(this.selectedVehicle.id)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.generating = false)))
      .subscribe({
        next: () => {
          this.loadActiveToken();
          this.loadHistory();
        },
        error: () =>
          (this.generateError = 'No se pudo generar el código de transferencia. Intentá de nuevo.'),
      });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  statusBadgeClass(status: TransferStatus): string {
    switch (status) {
      case 'PENDING':   return 'bg-amber-100 text-amber-700';
      case 'COMPLETED': return 'bg-emerald-100 text-emerald-700';
      case 'EXPIRED':   return 'bg-red-100 text-red-700';
    }
  }

  statusLabel(status: TransferStatus): string {
    switch (status) {
      case 'PENDING':   return '● Pendiente';
      case 'COMPLETED': return '● Completada';
      case 'EXPIRED':   return '● Vencida';
    }
  }

  hoursRemaining(expiresAt: string): number {
    return Math.max(0, Math.round((new Date(expiresAt).getTime() - Date.now()) / 3_600_000));
  }

  formatDateTime(value: string): string {
    return new Date(value).toLocaleString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
    });
  }

  trackByTokenId(_: number, item: TransferHistoryItemResponse): number {
    return item.tokenId;
  }
}
