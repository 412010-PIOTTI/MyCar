import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  constructor(private router: Router, private authService: AuthService) {}

  isActive(path: string): boolean {
    return this.router.url.startsWith(path);
  }

  logout(): void {
    this.authService.logout();
  }
}
