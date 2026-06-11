import { Component, DestroyRef, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { ExpenseService } from '../../../core/services/expense.service';
import { ExpenseCategory, ExpenseResponse } from '../../../core/models/expense.model';

interface CategoryOption {
  value: ExpenseCategory;
  label: string;
  icon: string;
  subcategoryHint: string;
}

@Component({
  selector: 'app-expense-register-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      (click)="onBackdropClick()"
    >
      <div
        class="bg-surface-card rounded-lg shadow-modal w-full max-w-lg max-h-[90vh] overflow-y-auto"
        (click)="$event.stopPropagation()"
      >
        <!-- Header -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-surface-border">
          <h2 class="text-lg font-semibold text-text-primary">Registrar gasto</h2>
          <button
            type="button"
            (click)="close.emit()"
            [disabled]="loading"
            class="text-text-secondary hover:text-text-primary transition-colors disabled:opacity-40"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
            </svg>
          </button>
        </div>

        <!-- Form -->
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="px-6 py-5 space-y-4">

          <!-- Category -->
          <div>
            <label class="block text-sm font-medium text-text-primary mb-1">Categoría <span class="text-danger">*</span></label>
            <select formControlName="category" class="form-input w-full">
              <option value="" disabled>Seleccioná una categoría</option>
              <option *ngFor="let cat of categoryOptions" [value]="cat.value">
                {{ cat.icon }} {{ cat.label }}
              </option>
            </select>
            <p *ngIf="fieldError('category')" class="mt-1 text-xs text-danger">{{ fieldError('category') }}</p>
          </div>

          <!-- Subcategory -->
          <div>
            <label class="block text-sm font-medium text-text-primary mb-1">Subcategoría</label>
            <input
              type="text"
              formControlName="subcategory"
              class="form-input w-full"
              [placeholder]="activeCategoryHint"
              maxlength="50"
            />
            <p *ngIf="fieldError('subcategory')" class="mt-1 text-xs text-danger">{{ fieldError('subcategory') }}</p>
          </div>

          <!-- Date + Amount -->
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-text-primary mb-1">Fecha <span class="text-danger">*</span></label>
              <input type="date" formControlName="date" class="form-input w-full" [max]="today" />
              <p *ngIf="fieldError('date')" class="mt-1 text-xs text-danger">{{ fieldError('date') }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-text-primary mb-1">Monto <span class="text-danger">*</span></label>
              <input
                type="number"
                formControlName="amount"
                class="form-input w-full"
                placeholder="0.00"
                step="0.01"
                min="0.01"
              />
              <p *ngIf="fieldError('amount')" class="mt-1 text-xs text-danger">{{ fieldError('amount') }}</p>
            </div>
          </div>

          <!-- Description -->
          <div>
            <label class="block text-sm font-medium text-text-primary mb-1">Descripción</label>
            <textarea
              formControlName="description"
              rows="2"
              class="form-input w-full resize-none"
              placeholder="Detalles adicionales del gasto..."
              maxlength="300"
            ></textarea>
            <p *ngIf="fieldError('description')" class="mt-1 text-xs text-danger">{{ fieldError('description') }}</p>
          </div>

          <!-- KM + Expiry date -->
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-text-primary mb-1">Kilometraje</label>
              <input
                type="number"
                formControlName="kmAtExpense"
                class="form-input w-full"
                placeholder="Km actual"
                min="0"
              />
              <p *ngIf="fieldError('kmAtExpense')" class="mt-1 text-xs text-danger">{{ fieldError('kmAtExpense') }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-text-primary mb-1">Fecha de vencimiento</label>
              <input type="date" formControlName="expiryDate" class="form-input w-full" />
              <p *ngIf="fieldError('expiryDate')" class="mt-1 text-xs text-danger">{{ fieldError('expiryDate') }}</p>
            </div>
          </div>

          <!-- General error -->
          <p *ngIf="generalError" class="text-sm text-danger bg-danger-light rounded px-3 py-2">{{ generalError }}</p>

          <!-- Actions -->
          <div class="flex justify-end gap-3 pt-2">
            <button type="button" (click)="close.emit()" [disabled]="loading" class="btn-secondary">
              Cancelar
            </button>
            <button type="submit" [disabled]="loading" class="btn-primary min-w-[120px]">
              <span *ngIf="!loading">Registrar</span>
              <span *ngIf="loading" class="flex items-center justify-center gap-2">
                <svg class="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
                </svg>
                Guardando...
              </span>
            </button>
          </div>

        </form>
      </div>
    </div>
  `,
})
export class ExpenseRegisterModalComponent implements OnInit {
  @Input({ required: true }) vehicleId!: number;
  @Output() close = new EventEmitter<void>();
  @Output() expenseCreated = new EventEmitter<ExpenseResponse>();

  private fb = inject(FormBuilder);
  private expenseService = inject(ExpenseService);
  private destroyRef = inject(DestroyRef);

  readonly today = new Date().toISOString().split('T')[0];

  readonly categoryOptions: CategoryOption[] = [
    { value: 'OPERATIVO',       label: 'Operativos',          icon: '⛽', subcategoryHint: 'Ej: Combustible, Peaje, Estacionamiento' },
    { value: 'MANTENIMIENTO',   label: 'Mantenimiento',        icon: '🔧', subcategoryHint: 'Ej: Cambio de aceite, Neumáticos, Frenos' },
    { value: 'IMPUESTO_SEGURO', label: 'Impuestos y seguros',  icon: '🏛', subcategoryHint: 'Ej: Patente, Seguro, VTV' },
    { value: 'INFRACCION',      label: 'Infracciones',         icon: '⚠️', subcategoryHint: 'Ej: Multa velocidad, Estacionamiento' },
    { value: 'MEJORA',          label: 'Mejoras',              icon: '✨', subcategoryHint: 'Ej: Equipo de audio, Accesorios' },
    { value: 'ADMINISTRATIVO',  label: 'Administrativo',       icon: '📋', subcategoryHint: 'Ej: Transferencia, Gestión' },
  ];

  form = this.fb.group({
    category:    ['' as ExpenseCategory | '', Validators.required],
    subcategory: ['', Validators.maxLength(50)],
    date:        [this.today, Validators.required],
    amount:      [null as number | null, [Validators.required, Validators.min(0.01)]],
    description: ['', Validators.maxLength(300)],
    kmAtExpense: [null as number | null, Validators.min(0)],
    expiryDate:  [''],
  });

  loading = false;
  generalError = '';

  get activeCategoryHint(): string {
    const selected = this.categoryOptions.find(c => c.value === this.form.value.category);
    return selected?.subcategoryHint ?? 'Ej: Combustible, Peaje...';
  }

  ngOnInit(): void {}

  fieldError(name: string): string {
    const ctrl = this.form.get(name);
    if (!ctrl?.invalid || !ctrl.touched) return '';
    if (ctrl.errors?.['serverError']) return ctrl.errors['serverError'];
    if (ctrl.errors?.['required']) return 'Este campo es obligatorio.';
    if (ctrl.errors?.['maxlength']) return `Máximo ${ctrl.errors['maxlength'].requiredLength} caracteres.`;
    if (ctrl.errors?.['min']) return `El valor mínimo es ${ctrl.errors['min'].min}.`;
    return '';
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.generalError = '';

    const v = this.form.value;
    this.expenseService
      .createExpense(this.vehicleId, {
        category:    v.category as ExpenseCategory,
        subcategory: v.subcategory || null,
        date:        v.date!,
        amount:      v.amount!,
        description: v.description || null,
        kmAtExpense: v.kmAtExpense ?? null,
        expiryDate:  v.expiryDate || null,
      })
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.loading = false)))
      .subscribe({
        next: (expense) => this.expenseCreated.emit(expense),
        error: (err) => {
          if (err.status === 400 && err.error?.errors) {
            const errors = err.error.errors as Record<string, string>;
            Object.keys(errors).forEach((field) => this.form.get(field)?.setErrors({ serverError: errors[field] }));
          } else {
            this.generalError = 'No se pudo registrar el gasto. Intentá de nuevo.';
          }
        },
      });
  }

  onBackdropClick(): void {
    if (!this.loading) this.close.emit();
  }
}
