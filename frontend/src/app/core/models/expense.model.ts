export type ExpenseCategory =
  | 'OPERATIVO'
  | 'MANTENIMIENTO'
  | 'IMPUESTO_SEGURO'
  | 'INFRACCION'
  | 'MEJORA'
  | 'ADMINISTRATIVO';

export type ExpenseStatus = 'VIGENTE' | 'POR_VENCER' | 'VENCIDO';

export interface ExpenseResponse {
  id: number;
  vehicleId: number;
  category: ExpenseCategory;
  subcategory: string | null;
  date: string;
  amount: number;
  description: string | null;
  kmAtExpense: number | null;
  expiryDate: string | null;
  status: ExpenseStatus | null;
  createdAt: string;
}

export interface CreateExpenseRequest {
  category: ExpenseCategory;
  subcategory?: string | null;
  date: string;
  amount: number;
  description?: string | null;
  kmAtExpense?: number | null;
  expiryDate?: string | null;
}

export interface ExpenseSummaryResponse {
  totalCurrentMonth: number;
  totalPreviousMonth: number;
  percentageChange: number | null;
  byCategory: Partial<Record<ExpenseCategory, number>>;
}

export interface MonthlyTotalResponse {
  month: number;
  total: number;
}
