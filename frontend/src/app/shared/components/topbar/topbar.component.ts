import { Component, DestroyRef, Input, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <header
      class="flex items-center justify-between px-6 bg-white border-b border-surface-border flex-shrink-0"
      style="height: 64px">

      <!-- Page title -->
      <h1 class="text-lg font-semibold text-text-primary">{{ pageTitle }}</h1>

      <!-- Right side -->
      <div class="flex items-center gap-1">

        <button
          title="Notificaciones"
          class="p-2 text-text-secondary hover:text-text-primary rounded-md hover:bg-gray-50 transition-colors">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
          </svg>
        </button>

        <button
          title="Ayuda"
          class="p-2 text-text-secondary hover:text-text-primary rounded-md hover:bg-gray-50 transition-colors">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </button>

        <div class="w-px h-6 bg-surface-border mx-2"></div>

        <!-- User avatar + name -->
        <div class="flex items-center gap-2 pl-1">
          <div
            class="w-8 h-8 rounded-full bg-primary flex items-center justify-center flex-shrink-0 select-none">
            <span class="text-white text-xs font-semibold">{{ initials }}</span>
          </div>
          @if (userName) {
            <span class="text-sm font-medium text-text-primary">{{ userName }}</span>
          }
        </div>

      </div>
    </header>
  `,
})
export class TopbarComponent implements OnInit {
  @Input() pageTitle = '';

  userName = '';
  initials = '';

  private destroyRef = inject(DestroyRef);

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.userService
      .getMe()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (user) => {
          this.userName = user.name;
          this.initials = this.buildInitials(user.name);
        },
      });
  }

  private buildInitials(name: string): string {
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return name.substring(0, 2).toUpperCase();
  }
}
