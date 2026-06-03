import { Component } from '@angular/core';
import { UnderConstructionComponent } from '../../shared/components/under-construction/under-construction.component';

@Component({
  selector: 'app-transfers',
  standalone: true,
  imports: [UnderConstructionComponent],
  template: `<app-under-construction moduleName="Transferencias"></app-under-construction>`,
})
export class TransfersComponent {}
