import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UrgencyLevel } from '../../../core/models/alert.model';

const CLASSES: Record<UrgencyLevel, string> = {
  URGENTE: 'bg-red-100 text-danger',
  ADVERTENCIA: 'bg-amber-100 text-warning',
  INFORMATIVA: 'bg-info-light text-primary',
};

const LABELS: Record<UrgencyLevel, string> = {
  URGENTE: 'Urgente',
  ADVERTENCIA: 'Advertencia',
  INFORMATIVA: 'Informativa',
};

@Component({
  selector: 'app-urgency-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span
      class="inline-flex items-center px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wide whitespace-nowrap"
      [class]="CLASSES[level]">
      {{ LABELS[level] }}
    </span>
  `,
})
export class UrgencyBadgeComponent {
  @Input({ required: true }) level!: UrgencyLevel;

  protected readonly CLASSES = CLASSES;
  protected readonly LABELS = LABELS;
}
