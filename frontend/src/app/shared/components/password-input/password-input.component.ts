import { Component, Input } from '@angular/core';
import { ReactiveFormsModule, FormControl } from '@angular/forms';

@Component({
  selector: 'app-password-input',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="relative">
      <input
        [id]="inputId"
        [type]="show ? 'text' : 'password'"
        [formControl]="control"
        [placeholder]="placeholder"
        class="block w-full rounded-lg border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 placeholder-slate-400 focus:border-auth-accent focus:outline-none focus:ring-1 focus:ring-auth-accent pr-12 transition-colors"
        [class.border-red-400]="control.invalid && control.touched"
        [class.focus:ring-red-400]="control.invalid && control.touched"
      />
      <button
        type="button"
        (click)="show = !show"
        class="absolute inset-y-0 right-0 flex items-center px-3 text-slate-400 hover:text-slate-600 transition-colors"
        tabindex="-1"
        [attr.aria-label]="show ? 'Ocultar contraseña' : 'Mostrar contraseña'"
      >
        @if (!show) {
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
            <circle cx="12" cy="12" r="3"/>
          </svg>
        } @else {
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94
              M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19
              m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
            <line x1="1" y1="1" x2="23" y2="23"/>
          </svg>
        }
      </button>
    </div>
  `,
})
export class PasswordInputComponent {
  @Input({ required: true }) control!: FormControl<string | null>;
  @Input() inputId = 'password';
  @Input() placeholder = '••••••••';
  show = false;
}
