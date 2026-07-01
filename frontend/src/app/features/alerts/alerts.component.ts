import { Component } from '@angular/core';
import { AlertsListComponent } from './alerts-list/alerts-list.component';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [AlertsListComponent],
  template: `<app-alerts-list />`,
})
export class AlertsComponent {}
