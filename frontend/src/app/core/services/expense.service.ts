import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateExpenseRequest,
  ExpenseCategory,
  ExpenseResponse,
  ExpenseSummaryResponse,
  MonthlyTotalResponse,
} from '../models/expense.model';

@Injectable({ providedIn: 'root' })
export class ExpenseService {
  private http = inject(HttpClient);

  private base(vehicleId: number): string {
    return `${environment.apiUrl}/vehicles/${vehicleId}/expenses`;
  }

  getExpenses(vehicleId: number, category?: ExpenseCategory | null): Observable<ExpenseResponse[]> {
    const params = category ? new HttpParams().set('category', category) : undefined;
    return this.http.get<ExpenseResponse[]>(this.base(vehicleId), { params });
  }

  getExpenseById(vehicleId: number, expenseId: number): Observable<ExpenseResponse> {
    return this.http.get<ExpenseResponse>(`${this.base(vehicleId)}/${expenseId}`);
  }

  createExpense(vehicleId: number, data: CreateExpenseRequest): Observable<ExpenseResponse> {
    return this.http.post<ExpenseResponse>(this.base(vehicleId), data);
  }

  getSummary(vehicleId: number, year: number, month: number): Observable<ExpenseSummaryResponse> {
    const params = new HttpParams().set('year', year).set('month', month);
    return this.http.get<ExpenseSummaryResponse>(`${this.base(vehicleId)}/summary`, { params });
  }

  getMonthlyTotals(vehicleId: number, year: number): Observable<MonthlyTotalResponse[]> {
    const params = new HttpParams().set('year', year);
    return this.http.get<MonthlyTotalResponse[]>(`${this.base(vehicleId)}/monthly-totals`, { params });
  }

  getAllExpenses(category?: ExpenseCategory | null): Observable<ExpenseResponse[]> {
    const params = category ? new HttpParams().set('category', category) : undefined;
    return this.http.get<ExpenseResponse[]>(`${environment.apiUrl}/expenses`, { params });
  }

  getAllSummary(year: number, month: number): Observable<ExpenseSummaryResponse> {
    const params = new HttpParams().set('year', year).set('month', month);
    return this.http.get<ExpenseSummaryResponse>(`${environment.apiUrl}/expenses/summary`, { params });
  }

  getAllMonthlyTotals(year: number): Observable<MonthlyTotalResponse[]> {
    const params = new HttpParams().set('year', year);
    return this.http.get<MonthlyTotalResponse[]>(`${environment.apiUrl}/expenses/monthly-totals`, { params });
  }
}
