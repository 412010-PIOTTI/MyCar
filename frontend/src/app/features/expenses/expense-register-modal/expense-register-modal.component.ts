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
  templateUrl: './expense-register-modal.component.html',
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
