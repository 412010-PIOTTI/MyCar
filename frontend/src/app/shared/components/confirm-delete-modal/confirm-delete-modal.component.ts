import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { PasswordInputComponent } from '../password-input/password-input.component';

@Component({
  selector: 'app-confirm-delete-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, PasswordInputComponent],
  template: `
    @if (visible) {
      <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
           (click)="onBackdropClick($event)">
        <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6" (click)="$event.stopPropagation()">

          <!-- Header -->
          <div class="flex items-center gap-3 mb-4">
            <div class="flex-shrink-0 w-10 h-10 rounded-full bg-danger-light flex items-center justify-center">
              <svg class="w-5 h-5 text-danger" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
            <div>
              <h2 class="text-base font-bold text-text-primary">{{ title }}</h2>
              <p class="text-xs text-text-secondary">{{ subtitle }}</p>
            </div>
          </div>

          <p class="text-sm text-text-secondary mb-5">
            {{ message }}
          </p>

          <!-- Password field -->
          <div class="mb-4">
            <label class="block text-xs font-medium text-text-secondary mb-1.5">
              Contraseña actual
            </label>
            <app-password-input
              [control]="passwordControl"
              inputId="delete-password"
              placeholder="Tu contraseña actual"
            />
            @if (passwordControl.invalid && passwordControl.touched) {
              <p class="text-xs text-danger mt-1">La contraseña es requerida.</p>
            }
          </div>

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
              (click)="onCancel()"
              [disabled]="loading"
              class="px-4 py-2 text-sm font-medium text-text-secondary border border-surface-border
                     rounded-md hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
              Cancelar
            </button>
            <button
              type="button"
              (click)="onConfirm()"
              [disabled]="loading || passwordControl.invalid"
              class="px-4 py-2 text-sm font-semibold text-white bg-danger rounded-md
                     hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed
                     flex items-center gap-2">
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
export class ConfirmDeleteModalComponent implements OnChanges {
  @Input() visible = false;
  @Input() loading = false;
  @Input() errorMessage = '';
  @Input() title = 'Solicitar Baja de Cuenta';
  @Input() subtitle = 'Esta acción desactivará tu cuenta.';
  @Input() message = 'Tu cuenta será desactivada y no podrás acceder nuevamente. Todos tus datos quedan guardados en nuestra base de datos. Para confirmar, ingresá tu contraseña actual.';
  @Input() confirmLabel = 'Confirmar Baja';

  @Output() cancelled = new EventEmitter<void>();
  @Output() confirmed = new EventEmitter<string>();

  passwordControl = new FormControl<string | null>('', Validators.required);

  ngOnChanges(): void {
    if (!this.visible) {
      this.passwordControl.reset('');
    }
  }

  onBackdropClick(event: MouseEvent): void {
    if (!this.loading) this.cancelled.emit();
  }

  onCancel(): void {
    if (!this.loading) this.cancelled.emit();
  }

  onConfirm(): void {
    this.passwordControl.markAsTouched();
    if (this.passwordControl.invalid) return;
    this.confirmed.emit(this.passwordControl.value ?? '');
  }
}
