import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, forkJoin } from 'rxjs';
import { VehicleService } from '../../../core/services/vehicle.service';
import { ExpenseService } from '../../../core/services/expense.service';
import { AuthService } from '../../../core/services/auth.service';
import { Vehicle } from '../../../core/models/vehicle.model';
import {
  ExpenseCategory,
  ExpenseResponse,
  ExpenseSummaryResponse,
  MonthlyTotalResponse,
} from '../../../core/models/expense.model';
import { ExpenseRegisterModalComponent } from '../expense-register-modal/expense-register-modal.component';

interface TabOption {
  label: string;
  value: ExpenseCategory | null;
}

interface CategoryMeta {
  icon: string;
  bgColor: string;
  label: string;
}

@Component({
  selector: 'app-expenses-list',
  standalone: true,
  imports: [CommonModule, ExpenseRegisterModalComponent],
  templateUrl: './expenses-list.component.html',
})
export class ExpensesListComponent implements OnInit {
  private destroyRef     = inject(DestroyRef);
  private vehicleService = inject(VehicleService);
  private expenseService = inject(ExpenseService);
  private authService    = inject(AuthService);

  // ── Vehicle selector ──────────────────────────────────────────────────────
  vehicles: Vehicle[]      = [];
  selectedVehicle: Vehicle | null = null;
  vehiclesLoading = true;

  // ── Data ──────────────────────────────────────────────────────────────────
  expenses: ExpenseResponse[]          = [];
  summary: ExpenseSummaryResponse | null = null;
  currentYearTotals: MonthlyTotalResponse[] = [];
  prevYearTotals: MonthlyTotalResponse[]    = [];

  loading     = false;
  listLoading = false;
  loadError   = false;

  // ── Tabs ──────────────────────────────────────────────────────────────────
  activeCategory: ExpenseCategory | null = null;

  readonly tabs: TabOption[] = [
    { label: 'Todos',           value: null },
    { label: 'Operativos',      value: 'OPERATIVO' },
    { label: 'Mantenimiento',   value: 'MANTENIMIENTO' },
    { label: 'Impuestos',       value: 'IMPUESTO_SEGURO' },
    { label: 'Infracciones',    value: 'INFRACCION' },
  ];

  // ── Modal ─────────────────────────────────────────────────────────────────
  showRegisterModal = false;

  // ── Chart ─────────────────────────────────────────────────────────────────
  readonly currentYear = new Date().getFullYear();
  readonly months      = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
  readonly monthNames  = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];

  // ── Category meta ─────────────────────────────────────────────────────────
  private readonly categoryMeta: Record<string, CategoryMeta> = {
    OPERATIVO:       { icon: '⛽', bgColor: 'bg-emerald-100',  label: 'Operativos' },
    MANTENIMIENTO:   { icon: '🔧', bgColor: 'bg-amber-100',    label: 'Mantenimiento' },
    IMPUESTO_SEGURO: { icon: '🏛', bgColor: 'bg-blue-100',     label: 'Impuestos' },
    INFRACCION:      { icon: '⚠️', bgColor: 'bg-red-100',      label: 'Infracciones' },
    MEJORA:          { icon: '✨', bgColor: 'bg-purple-100',   label: 'Mejoras' },
    ADMINISTRATIVO:  { icon: '📋', bgColor: 'bg-slate-100',    label: 'Administrativo' },
  };

  ngOnInit(): void {
    this.vehicleService
      .getVehicles()
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.vehiclesLoading = false)))
      .subscribe({
        next: (vehicles) => {
          this.vehicles = vehicles;
          if (vehicles.length === 1) this.selectVehicle(vehicles[0]);
        },
        error: () => { this.loadError = true; this.vehiclesLoading = false; },
      });
  }

  selectVehicle(vehicle: Vehicle): void {
    this.selectedVehicle = vehicle;
    this.activeCategory  = null;
    this.loadAll();
  }

  onVehicleChange(event: Event): void {
    const id = Number((event.target as HTMLSelectElement).value);
    const vehicle = this.vehicles.find((v) => v.id === id);
    if (vehicle) this.selectVehicle(vehicle);
  }

  selectTab(category: ExpenseCategory | null): void {
    if (this.activeCategory === category) return;
    this.activeCategory = category;
    this.reloadExpenses();
  }

  trackById(_: number, expense: ExpenseResponse): number { return expense.id; }

  loadAll(): void {
    if (!this.selectedVehicle || !this.authService.isAuthenticated()) return;
    const id    = this.selectedVehicle.id;
    const now   = new Date();
    const year  = now.getFullYear();
    const month = now.getMonth() + 1;

    this.loading   = true;
    this.loadError = false;

    forkJoin({
      expenses:    this.expenseService.getExpenses(id, this.activeCategory),
      summary:     this.expenseService.getSummary(id, year, month),
      currentYear: this.expenseService.getMonthlyTotals(id, year),
      prevYear:    this.expenseService.getMonthlyTotals(id, year - 1),
    })
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.loading = false)))
      .subscribe({
        next: ({ expenses, summary, currentYear, prevYear }) => {
          this.expenses         = expenses;
          this.summary          = summary;
          this.currentYearTotals = currentYear;
          this.prevYearTotals    = prevYear;
        },
        error: () => (this.loadError = true),
      });
  }

  private reloadExpenses(): void {
    if (!this.selectedVehicle) return;
    this.listLoading = true;
    this.expenseService
      .getExpenses(this.selectedVehicle.id, this.activeCategory)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.listLoading = false)))
      .subscribe({ next: (expenses) => (this.expenses = expenses) });
  }

  onExpenseCreated(expense: ExpenseResponse): void {
    this.showRegisterModal = false;
    // Refresh all data so summary and chart stay in sync
    this.loadAll();
  }

  // ── Chart helpers ─────────────────────────────────────────────────────────
  get chartMaxValue(): number {
    const all = [
      ...this.currentYearTotals.map((t) => t.total),
      ...this.prevYearTotals.map((t) => t.total),
    ];
    return Math.max(...all, 1);
  }

  barHeightPercent(totals: MonthlyTotalResponse[], month: number): number {
    const entry = totals.find((t) => t.month === month);
    if (!entry || entry.total === 0) return 0;
    return Math.round((entry.total / this.chartMaxValue) * 100);
  }

  monthTotal(totals: MonthlyTotalResponse[], month: number): number {
    return totals.find((t) => t.month === month)?.total ?? 0;
  }

  // ── Category helpers ──────────────────────────────────────────────────────
  getCategoryIcon(category: string): string {
    return this.categoryMeta[category]?.icon ?? '💰';
  }

  getCategoryBg(category: string): string {
    return this.categoryMeta[category]?.bgColor ?? 'bg-gray-100';
  }

  getCategoryLabel(category: string): string {
    return this.categoryMeta[category]?.label ?? category;
  }

  getExpenseTitle(expense: ExpenseResponse): string {
    return expense.subcategory || this.getCategoryLabel(expense.category);
  }

  // ── Summary helpers ───────────────────────────────────────────────────────
  get trendIsPositive(): boolean {
    return (this.summary?.percentageChange ?? 0) >= 0;
  }

  categoryTotal(category: ExpenseCategory): number {
    return this.summary?.byCategory?.[category] ?? 0;
  }
}
