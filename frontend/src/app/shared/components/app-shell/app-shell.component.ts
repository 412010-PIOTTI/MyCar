import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { TopbarComponent } from '../topbar/topbar.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [SidebarComponent, TopbarComponent, RouterOutlet],
  template: `
    <div class="flex h-screen bg-surface overflow-hidden">
      <app-sidebar [mobileOpen]="sidebarMobileOpen" (mobileClose)="sidebarMobileOpen = false"></app-sidebar>
      <div class="flex flex-col flex-1 min-w-0">
        <app-topbar [pageTitle]="pageTitle" [helpText]="helpText" (menuToggle)="sidebarMobileOpen = !sidebarMobileOpen"></app-topbar>
        <main class="flex-1 overflow-y-auto">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
})
export class AppShellComponent implements OnInit {
  pageTitle = '';
  helpText = '';
  sidebarMobileOpen = false;

  private destroyRef = inject(DestroyRef);

  constructor(private router: Router, private activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.updateRouteData();
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(() => {
      this.updateRouteData();
      this.sidebarMobileOpen = false;
    });
  }

  private updateRouteData(): void {
    let route = this.activatedRoute;
    while (route.firstChild) route = route.firstChild;
    this.pageTitle = this.findRouteData(route, 'title');
    this.helpText = this.findRouteData(route, 'help');
  }

  // Child routes inherit title/help from the nearest ancestor that defines them
  private findRouteData(route: ActivatedRoute, key: string): string {
    let r: ActivatedRoute | null = route;
    while (r) {
      const value = r.snapshot.data[key];
      if (value) return value;
      r = r.parent;
    }
    return '';
  }
}
