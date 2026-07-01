import { Component, DestroyRef, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { MaintenanceService } from '../../../core/services/maintenance.service';
import { MaintenanceSystem } from '../../../core/models/maintenance.model';

interface SystemOption {
  value: MaintenanceSystem;
  label: string;
}

@Component({
  selector: 'app-maintenance-register-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './maintenance-register-modal.component.html',
})
export class MaintenanceRegisterModalComponent {
  @Input({ required: true }) vehicleId!: number;
  @Output() close      = new EventEmitter<void>();
  @Output() logCreated = new EventEmitter<void>();

  private fb             = inject(FormBuilder);
  private maintenanceSvc = inject(MaintenanceService);
  private destroyRef     = inject(DestroyRef);

  readonly today = (() => {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  })();

  readonly systemOptions: SystemOption[] = [
    { value: 'MOTOR',       label: 'Motor' },
    { value: 'TRANSMISION', label: 'Transmisión' },
    { value: 'FRENOS',      label: 'Frenos' },
    { value: 'ELECTRICO',   label: 'Eléctrico' },
    { value: 'SUSPENSION',  label: 'Suspensión' },
    { value: 'CARROCERIA',  label: 'Carrocería' },
    { value: 'OTRO',        label: 'Otro / General' },
  ];

  form = this.fb.group({
    system:             ['' as MaintenanceSystem | '', Validators.required],
    date:               [this.today, Validators.required],
    kmAtMaintenance:    [null as number | null, [Validators.required, Validators.min(0)]],
    workshop:           ['', [Validators.required, Validators.maxLength(100)]],
    description:        ['', Validators.maxLength(300)],
    cost:               [null as number | null],
    nextServiceKm:      [null as number | null, Validators.min(0)],
    nextServiceDate:    [''],
    createExpense:      [false],
    expenseSubcategory: ['', Validators.maxLength(50)],
  });

  get wantsExpense(): boolean { return !!this.form.get('createExpense')?.value; }

  loading      = false;
  generalError = '';

  fieldError(name: string): string {
    const ctrl = this.form.get(name);
    if (!ctrl?.invalid || !ctrl.touched) return '';
    if (ctrl.errors?.['serverError']) return ctrl.errors['serverError'];
    if (ctrl.errors?.['required'])    return 'Este campo es obligatorio.';
    if (ctrl.errors?.['maxlength'])   return `Máximo ${ctrl.errors['maxlength'].requiredLength} caracteres.`;
    if (ctrl.errors?.['min'])         return `El valor mínimo es ${ctrl.errors['min'].min}.`;
    return '';
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading      = true;
    this.generalError = '';

    const v = this.form.value;
    this.maintenanceSvc
      .createMaintenanceLog(this.vehicleId, {
        system:             v.system as MaintenanceSystem,
        date:               v.date!,
        kmAtMaintenance:    v.kmAtMaintenance!,
        workshop:           v.workshop!,
        description:        v.description || null,
        cost:               v.cost ?? null,
        nextServiceKm:      v.nextServiceKm ?? null,
        nextServiceDate:    v.nextServiceDate || null,
        createExpense:      !!v.createExpense && v.cost != null,
        expenseSubcategory: v.createExpense && v.expenseSubcategory ? v.expenseSubcategory : null,
      })
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.loading = false)))
      .subscribe({
        next: () => this.logCreated.emit(),
        error: (err) => {
          if (err.status === 400 && err.error?.errors) {
            const errors = err.error.errors as Record<string, string>;
            Object.keys(errors).forEach((field) => this.form.get(field)?.setErrors({ serverError: errors[field] }));
          } else {
            this.generalError = 'No se pudo registrar el service. Intentá de nuevo.';
          }
        },
      });
  }

  onBackdropClick(): void {
    if (!this.loading) this.close.emit();
  }
}
