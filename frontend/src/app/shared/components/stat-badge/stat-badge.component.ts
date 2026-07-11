import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-stat-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div
      class="rounded-md shadow-card px-5 py-3 flex flex-col gap-0.5"
      [class]="highlight
        ? 'bg-info-light border-2 border-secondary'
        : 'bg-white border border-surface-border'">
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
  /** Adds an accent border/background so this badge stands out among the others. */
  @Input() highlight = false;
}
