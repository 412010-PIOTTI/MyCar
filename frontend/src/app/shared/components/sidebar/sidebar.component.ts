import { Component } from '@angular/core';
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
  collapsed = true;
  showLogoutModal = false;

  constructor(private router: Router) {}

  isActive(path: string): boolean {
    return this.router.url.startsWith(path);
  }

  toggleCollapsed(): void {
    this.collapsed = !this.collapsed;
  }

  openLogoutModal(): void {
    this.showLogoutModal = true;
  }

  closeLogoutModal(): void {
    this.showLogoutModal = false;
  }
}
