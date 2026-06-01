import { Component } from '@angular/core';

@Component({
  selector: 'app-auth-layout',
  standalone: true,
  template: `
    <div class="flex min-h-screen font-sans">
      <aside class="hidden lg:flex flex-col w-2/5 bg-auth-panel overflow-hidden relative">
        <ng-content select="[left]" />
      </aside>
      <main class="flex-1 bg-white flex flex-col min-h-screen">
        <ng-content select="[right]" />
      </main>
    </div>
  `,
})
export class AuthLayoutComponent {}
