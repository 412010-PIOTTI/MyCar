import { Component } from '@angular/core';
import { UnderConstructionComponent } from '../../shared/components/under-construction/under-construction.component';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [UnderConstructionComponent],
  template: `<app-under-construction moduleName="Alertas"></app-under-construction>`,
})
export class AlertsComponent {}
