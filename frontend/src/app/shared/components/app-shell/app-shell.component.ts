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
      <app-sidebar></app-sidebar>
      <div class="flex flex-col flex-1 min-w-0">
        <app-topbar [pageTitle]="pageTitle"></app-topbar>
        <main class="flex-1 overflow-y-auto">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
})
export class AppShellComponent implements OnInit {
  pageTitle = '';

  private destroyRef = inject(DestroyRef);

  constructor(private router: Router, private activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.updateTitle();
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(() => this.updateTitle());
  }

  private updateTitle(): void {
    let route = this.activatedRoute;
    while (route.firstChild) route = route.firstChild;
    // Walk up until we find a title (child routes inherit from parent)
    let r: ActivatedRoute | null = route;
    while (r) {
      const title = r.snapshot.data['title'];
      if (title) { this.pageTitle = title; return; }
      r = r.parent;
    }
    this.pageTitle = '';
  }
}
