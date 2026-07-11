import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { LogoutConfirmModalComponent } from '../logout-confirm-modal/logout-confirm-modal.component';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, LogoutConfirmModalComponent],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  @Input() mobileOpen = false;
  @Output() mobileClose = new EventEmitter<void>();

  collapsed = true;
  showLogoutModal = false;

  constructor(private router: Router) {}

  isActive(path: string): boolean {
    return this.router.url.startsWith(path);
  }

  toggleCollapsed(): void {
    this.collapsed = !this.collapsed;
  }

  /** Full nav labels show when explicitly expanded, or when open as a mobile drawer. */
  get showLabels(): boolean {
    return !this.collapsed || this.mobileOpen;
  }

  onNavClick(): void {
    if (this.mobileOpen) this.mobileClose.emit();
  }

  openLogoutModal(): void {
    this.showLogoutModal = true;
  }

  closeLogoutModal(): void {
    this.showLogoutModal = false;
  }
}
