import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { VehicleService } from '../../../core/services/vehicle.service';
import { Vehicle } from '../../../core/models/vehicle.model';
import { VehicleRegisterModalComponent } from '../vehicle-register-modal/vehicle-register-modal.component';
import { VehicleEditModalComponent } from '../vehicle-edit-modal/vehicle-edit-modal.component';
import { ConfirmActionModalComponent } from '../../../shared/components/confirm-action-modal/confirm-action-modal.component';

@Component({
  selector: 'app-vehicles-list',
  standalone: true,
  imports: [CommonModule, RouterModule, VehicleRegisterModalComponent, VehicleEditModalComponent, ConfirmActionModalComponent],
  templateUrl: './vehicles-list.component.html',
})
export class VehiclesListComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private vehicleService = inject(VehicleService);

  vehicles: Vehicle[] = [];
  loading = true;
  loadError = false;

  showRegisterModal = false;
  vehicleToEdit: Vehicle | null = null;
  vehicleToDelete: Vehicle | null = null;
  deleteLoading = false;
  deleteError = '';

  ngOnInit(): void {
    this.loadVehicles();
  }

  loadVehicles(): void {
    this.loading = true;
    this.loadError = false;
    this.vehicleService
      .getVehicles()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (vehicles) => {
          this.vehicles = vehicles;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this.loadError = true;
        },
      });
  }

  onVehicleCreated(vehicle: Vehicle): void {
    this.vehicles = [vehicle, ...this.vehicles];
    this.showRegisterModal = false;
  }

  openEdit(vehicle: Vehicle): void {
    this.vehicleToEdit = vehicle;
  }

  onVehicleUpdated(updated: Vehicle): void {
    this.vehicles = this.vehicles.map((v) => (v.id === updated.id ? updated : v));
    this.vehicleToEdit = null;
  }

  openDelete(vehicle: Vehicle): void {
    this.vehicleToDelete = vehicle;
    this.deleteError = '';
  }

  closeDelete(): void {
    if (!this.deleteLoading) {
      this.vehicleToDelete = null;
      this.deleteError = '';
    }
  }

  onDeleteConfirmed(): void {
    if (!this.vehicleToDelete) return;
    this.deleteLoading = true;
    this.deleteError = '';
    const id = this.vehicleToDelete.id;

    this.vehicleService
      .deleteVehicle(id)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.deleteLoading = false)))
      .subscribe({
        next: () => {
          this.vehicles = this.vehicles.filter((v) => v.id !== id);
          this.vehicleToDelete = null;
        },
        error: () => {
          this.deleteError = 'No se pudo eliminar el vehículo. Intentá de nuevo.';
        },
      });
  }
}
