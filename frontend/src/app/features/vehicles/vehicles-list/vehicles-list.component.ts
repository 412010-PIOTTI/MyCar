import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { VehicleService, Vehicle } from '../../../core/services/vehicle.service';
import { VehicleRegisterModalComponent } from '../vehicle-register-modal/vehicle-register-modal.component';

@Component({
  selector: 'app-vehicles-list',
  standalone: true,
  imports: [CommonModule, RouterModule, VehicleRegisterModalComponent],
  templateUrl: './vehicles-list.component.html',
})
export class VehiclesListComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private vehicleService = inject(VehicleService);

  vehicles: Vehicle[] = [];
  loading = true;
  loadError = false;
  showRegisterModal = false;

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
}
