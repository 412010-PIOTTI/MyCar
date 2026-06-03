import { Component } from '@angular/core';
import { UnderConstructionComponent } from '../../shared/components/under-construction/under-construction.component';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [UnderConstructionComponent],
  template: `<app-under-construction moduleName="Documentos"></app-under-construction>`,
})
export class DocumentsComponent {}
