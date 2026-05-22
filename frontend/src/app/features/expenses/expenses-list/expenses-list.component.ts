import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-expenses-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './expenses-list.component.html',
})
export class ExpensesListComponent implements OnInit {
  expenses: any[] = [];

  ngOnInit(): void {
    // TODO: inyectar ExpensesService y cargar gastos
  }
}
