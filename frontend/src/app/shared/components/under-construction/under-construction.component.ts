import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-under-construction',
  standalone: true,
  template: `
    <div class="flex flex-col items-center justify-center min-h-full p-12 text-center">
      <div class="max-w-md">

        <div class="mx-auto mb-6 w-16 h-16 rounded-full bg-info-light flex items-center justify-center">
          <svg class="w-8 h-8 text-secondary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
              d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
          </svg>
        </div>

        <h2 class="text-xl font-bold text-text-primary mb-2 font-sans">
          {{ moduleName }} estará disponible próximamente
        </h2>
        <p class="text-sm text-text-secondary leading-relaxed font-sans">
          Estamos trabajando para traerte esta funcionalidad. Volvé pronto para ver las novedades.
        </p>

        <span class="inline-block mt-5 px-3 py-1 bg-info-light text-secondary text-xs font-medium rounded-full font-sans">
          En construcción
        </span>

      </div>
    </div>
  `,
})
export class UnderConstructionComponent {
  @Input() moduleName = '';
}
