import { Component } from '@angular/core';
import { SidebarComponent } from '../sidebar/sidebar.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [SidebarComponent],
  template: `
    <div class="flex h-screen bg-surface overflow-hidden">
      <app-sidebar></app-sidebar>
      <div class="flex flex-col flex-1 min-w-0">
        <main class="flex-1 overflow-y-auto">
          <ng-content></ng-content>
        </main>
      </div>
    </div>
  `,
})
export class AppShellComponent {}
