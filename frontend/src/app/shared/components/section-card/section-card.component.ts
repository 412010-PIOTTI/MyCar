import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-section-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-white rounded-md border border-surface-border shadow-card overflow-hidden">
      <div class="px-6 py-5 flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <h2 class="text-lg font-semibold text-primary">{{ title }}</h2>
        @if (subtitle) {
          <span class="text-xs font-semibold text-text-secondary tracking-widest uppercase">
            {{ subtitle }}
          </span>
        }
      </div>
      <hr class="border-surface-border">
      <div class="p-6">
        <ng-content></ng-content>
      </div>
    </div>
  `,
})
export class SectionCardComponent {
  @Input() title = '';
  @Input() subtitle = '';
}
