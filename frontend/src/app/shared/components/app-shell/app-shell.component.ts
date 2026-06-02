import { Component, Input } from '@angular/core';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { TopbarComponent } from '../topbar/topbar.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [SidebarComponent, TopbarComponent],
  template: `
    <div class="flex h-screen bg-surface overflow-hidden">
      <app-sidebar></app-sidebar>
      <div class="flex flex-col flex-1 min-w-0">
        @if (pageTitle) {
          <app-topbar [pageTitle]="pageTitle"></app-topbar>
        }
        <main class="flex-1 overflow-y-auto">
          <ng-content></ng-content>
        </main>
      </div>
    </div>
  `,
})
export class AppShellComponent {
  @Input() pageTitle = '';
}
