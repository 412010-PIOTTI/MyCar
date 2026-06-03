import { Component } from '@angular/core';
import { UnderConstructionComponent } from '../../shared/components/under-construction/under-construction.component';

@Component({
  selector: 'app-maintenance',
  standalone: true,
  imports: [UnderConstructionComponent],
  template: `<app-under-construction moduleName="Mantenimiento"></app-under-construction>`,
})
export class MaintenanceComponent {}
