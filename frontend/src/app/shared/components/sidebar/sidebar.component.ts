import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  loggingOut = false;
  collapsed = false;

  private destroyRef = inject(DestroyRef);

  constructor(private router: Router, private authService: AuthService) {}

  isActive(path: string): boolean {
    return this.router.url.startsWith(path);
  }

  toggleCollapsed(): void {
    this.collapsed = !this.collapsed;
  }

  logout(): void {
    if (this.loggingOut) return;
    this.loggingOut = true;
    this.authService
      .logout()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ error: () => (this.loggingOut = false) });
  }
}
