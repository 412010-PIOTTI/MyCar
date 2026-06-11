import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Generic danger-confirmation modal. No password required — use for
 * destructive actions that need a simple yes/no step (e.g. vehicle deletion).
 */
@Component({
  selector: 'app-confirm-action-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (visible) {
      <div
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
        (click)="onBackdropClick()"
      >
        <div
          class="bg-white rounded-xl shadow-modal w-full max-w-sm p-6"
          (click)="$event.stopPropagation()"
        >
          <!-- Icon + title -->
          <div class="flex items-center gap-3 mb-4">
            <div class="flex-shrink-0 w-10 h-10 rounded-full bg-danger-light flex items-center justify-center">
              <svg class="w-5 h-5 text-danger" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </div>
            <h2 class="text-base font-bold text-text-primary">{{ title }}</h2>
          </div>

          <p class="text-sm text-text-secondary mb-5">{{ message }}</p>

          <!-- Error message -->
          @if (errorMessage) {
            <div class="flex items-center gap-2 text-sm text-danger mb-4 bg-danger-light px-3 py-2 rounded">
              <svg class="w-4 h-4 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              {{ errorMessage }}
            </div>
          }

          <!-- Actions -->
          <div class="flex gap-3 justify-end">
            <button
              type="button"
              (click)="cancelled.emit()"
              [disabled]="loading"
              class="px-4 py-2 text-sm font-medium text-text-secondary border border-surface-border
                     rounded-md hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Cancelar
            </button>
            <button
              type="button"
              (click)="confirmed.emit()"
              [disabled]="loading"
              class="px-4 py-2 text-sm font-semibold text-white bg-danger rounded-md
                     hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed
                     flex items-center gap-2"
            >
              @if (loading) {
                <svg class="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
                </svg>
                Procesando...
              } @else {
                {{ confirmLabel }}
              }
            </button>
          </div>
        </div>
      </div>
    }
  `,
})
export class ConfirmActionModalComponent {
  @Input() visible = false;
  @Input() title = 'Confirmar acción';
  @Input() message = '¿Estás seguro de que querés continuar?';
  @Input() confirmLabel = 'Confirmar';
  @Input() loading = false;
  @Input() errorMessage = '';

  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  onBackdropClick(): void {
    if (!this.loading) this.cancelled.emit();
  }
}
