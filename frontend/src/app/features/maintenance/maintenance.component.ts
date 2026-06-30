import { Component } from '@angular/core';
import { MaintenanceListComponent } from './maintenance-list/maintenance-list.component';

@Component({
  selector: 'app-maintenance',
  standalone: true,
  imports: [MaintenanceListComponent],
  template: `<app-maintenance-list />`,
})
export class MaintenanceComponent {}
