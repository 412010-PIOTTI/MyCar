import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { VehicleService } from '../../../core/services/vehicle.service';
import { Vehicle } from '../../../core/models/vehicle.model';
import { VehicleEditModalComponent } from '../vehicle-edit-modal/vehicle-edit-modal.component';
import { ConfirmActionModalComponent } from '../../../shared/components/confirm-action-modal/confirm-action-modal.component';

@Component({
  selector: 'app-vehicle-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, VehicleEditModalComponent, ConfirmActionModalComponent],
  templateUrl: './vehicle-detail.component.html',
})
export class VehicleDetailComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private vehicleService = inject(VehicleService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  vehicle: Vehicle | null = null;
  loading = true;
  notFound = false;

  showEditModal = false;
  showDeleteModal = false;
  deleteLoading = false;
  deleteError = '';

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (isNaN(id)) {
      this.router.navigate(['/vehicles']);
      return;
    }
    this.vehicleService
      .getVehicleById(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (vehicle) => {
          this.vehicle = vehicle;
          this.loading = false;
        },
        error: (err) => {
          this.loading = false;
          this.notFound = err.status === 404;
        },
      });
  }

  onVehicleUpdated(updated: Vehicle): void {
    this.vehicle = updated;
    this.showEditModal = false;
  }

  onDeleteConfirmed(): void {
    if (!this.vehicle) return;
    this.deleteLoading = true;
    this.deleteError = '';

    this.vehicleService
      .deleteVehicle(this.vehicle.id)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.deleteLoading = false)))
      .subscribe({
        next: () => this.router.navigate(['/vehicles']),
        error: () => {
          this.deleteError = 'No se pudo eliminar el vehículo. Intentá de nuevo.';
        },
      });
  }
}
