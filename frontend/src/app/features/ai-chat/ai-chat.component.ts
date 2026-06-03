import { Component } from '@angular/core';
import { UnderConstructionComponent } from '../../shared/components/under-construction/under-construction.component';

@Component({
  selector: 'app-ai-chat',
  standalone: true,
  imports: [UnderConstructionComponent],
  template: `<app-under-construction moduleName="AI Chat"></app-under-construction>`,
})
export class AiChatComponent {}
