import { Component, DestroyRef, EventEmitter, Input, OnDestroy, Output, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-logout-confirm-modal',
  standalone: true,
  template: `
    @if (showToast) {
      <div class="fixed bottom-6 left-1/2 -translate-x-1/2 z-[9999]
                  flex items-center gap-2 px-4 py-3 bg-success text-text-inverse
                  rounded shadow-modal text-sm font-medium font-sans whitespace-nowrap">
        <svg class="w-4 h-4 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
        Sesión cerrada correctamente
      </div>
    }

    @if (visible && !showToast) {
      <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
           (click)="onCancel()">
        <div class="bg-white rounded-lg shadow-modal w-full max-w-sm p-6"
             (click)="$event.stopPropagation()">

          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 rounded-full bg-danger-light flex items-center justify-center flex-shrink-0">
              <svg class="w-5 h-5 text-danger" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
            </div>
            <h2 class="text-base font-bold text-text-primary font-sans">¿Cerrar sesión?</h2>
          </div>

          <p class="text-sm text-text-secondary mb-6 font-sans">
            Vas a salir de tu cuenta. Podés volver a iniciar sesión en cualquier momento.
          </p>

          <div class="flex gap-3 justify-end">
            <button
              type="button"
              (click)="onCancel()"
              class="px-4 py-2 text-sm font-medium text-text-secondary border border-surface-border
                     rounded hover:bg-gray-50 transition-colors">
              Cancelar
            </button>
            <button
              type="button"
              (click)="onConfirm()"
              class="px-4 py-2 text-sm font-semibold text-text-inverse bg-danger rounded
                     hover:bg-danger-dark transition-colors flex items-center gap-2">
              Cerrar sesión
            </button>
          </div>

        </div>
      </div>
    }
  `,
})
export class LogoutConfirmModalComponent implements OnDestroy {
  @Input() visible = false;
  @Output() cancelled = new EventEmitter<void>();

  showToast = false;
  private toastTimer: ReturnType<typeof setTimeout> | null = null;
  private destroyRef = inject(DestroyRef);

  constructor(private authService: AuthService) {}

  onCancel(): void {
    this.cancelled.emit();
  }

  onConfirm(): void {
    this.showToast = true;
    this.toastTimer = setTimeout(() => {
      this.authService
        .logout()
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe();
    }, 2000);
  }

  ngOnDestroy(): void {
    if (this.toastTimer !== null) {
      clearTimeout(this.toastTimer);
    }
  }
}
