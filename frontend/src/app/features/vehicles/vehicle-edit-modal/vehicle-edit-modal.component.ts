import { Component, DestroyRef, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { VehicleService } from '../../../core/services/vehicle.service';
import { Vehicle } from '../../../core/models/vehicle.model';

@Component({
  selector: 'app-vehicle-edit-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './vehicle-edit-modal.component.html',
})
export class VehicleEditModalComponent implements OnInit {
  @Input() vehicle!: Vehicle;
  @Output() close = new EventEmitter<void>();
  @Output() vehicleUpdated = new EventEmitter<Vehicle>();

  private fb = inject(FormBuilder);
  private vehicleService = inject(VehicleService);
  private destroyRef = inject(DestroyRef);

  readonly currentYear = new Date().getFullYear();

  form = this.fb.group({
    plate: ['', [Validators.required, Validators.maxLength(10)]],
    brand: ['', [Validators.required, Validators.maxLength(50)]],
    model: ['', [Validators.required, Validators.maxLength(50)]],
    year:  [null as number | null, [Validators.required, Validators.min(1900), Validators.max(this.currentYear)]],
    color: ['', Validators.maxLength(30)],
  });

  loading = false;
  generalError = '';

  ngOnInit(): void {
    this.form.patchValue({
      plate: this.vehicle.plate,
      brand: this.vehicle.brand,
      model: this.vehicle.model,
      year:  this.vehicle.year,
      color: this.vehicle.color ?? '',
    });
  }

  fieldError(name: string): string {
    const ctrl = this.form.get(name);
    if (!ctrl?.invalid || !ctrl.touched) return '';
    if (ctrl.errors?.['serverError'])  return ctrl.errors['serverError'];
    if (ctrl.errors?.['required'])     return 'Este campo es obligatorio.';
    if (ctrl.errors?.['maxlength'])    return `Máximo ${ctrl.errors['maxlength'].requiredLength} caracteres.`;
    if (ctrl.errors?.['min'])          return `El valor mínimo es ${ctrl.errors['min'].min}.`;
    if (ctrl.errors?.['max'])          return `El año no puede ser posterior a ${this.currentYear}.`;
    return '';
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.generalError = '';

    const { plate, brand, model, year, color } = this.form.value;

    this.vehicleService
      .updateVehicle(this.vehicle.id, {
        plate: plate!,
        brand: brand!,
        model: model!,
        year:  year!,
        color: color || null,
      })
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.loading = false)))
      .subscribe({
        next: (updated) => this.vehicleUpdated.emit(updated),
        error: (err) => {
          if (err.status === 409) {
            this.form.get('plate')?.setErrors({ serverError: 'Esta patente ya está registrada.' });
          } else if (err.status === 400 && err.error?.errors) {
            const errors = err.error.errors as Record<string, string>;
            Object.keys(errors).forEach((field) =>
              this.form.get(field)?.setErrors({ serverError: errors[field] })
            );
          } else {
            this.generalError = 'No se pudo actualizar el vehículo. Intentá de nuevo.';
          }
        },
      });
  }

  onBackdropClick(): void {
    if (!this.loading) this.close.emit();
  }
}
