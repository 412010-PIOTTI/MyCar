import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-stat-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-white rounded-md border border-surface-border shadow-card px-5 py-3 flex flex-col gap-0.5">
      <span class="text-xs font-medium text-text-secondary uppercase tracking-wide whitespace-nowrap">
        {{ label }}
      </span>
      <span class="text-xl font-bold text-text-primary whitespace-nowrap">
        {{ value }}
        @if (suffix) {
          <span class="text-sm font-semibold text-text-secondary ml-1">{{ suffix }}</span>
        }
      </span>
    </div>
  `,
})
export class StatBadgeComponent {
  @Input() label = '';
  @Input() value: string | number = '';
  @Input() suffix = '';
}
