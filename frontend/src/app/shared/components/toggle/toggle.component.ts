import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-toggle',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button
      type="button"
      role="switch"
      [attr.aria-checked]="checked"
      [disabled]="disabled"
      (click)="!disabled && onToggle()"
      class="relative inline-flex h-6 w-11 flex-shrink-0 rounded-full border-2 border-transparent
             transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2
             focus:ring-primary focus:ring-offset-2"
      [ngClass]="{
        'bg-primary': checked,
        'bg-gray-200': !checked,
        'opacity-50 cursor-not-allowed': disabled,
        'cursor-pointer': !disabled
      }">
      <span
        class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow
               ring-0 transition duration-200 ease-in-out"
        [ngClass]="checked ? 'translate-x-5' : 'translate-x-0'">
      </span>
    </button>
  `,
})
export class ToggleComponent {
  @Input() checked = false;
  @Input() disabled = false;
  @Output() checkedChange = new EventEmitter<boolean>();

  onToggle(): void {
    this.checked = !this.checked;
    this.checkedChange.emit(this.checked);
  }
}
