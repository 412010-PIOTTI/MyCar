import { Component } from '@angular/core';
import { AppShellComponent } from '../../shared/components/app-shell/app-shell.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [AppShellComponent],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent {}
