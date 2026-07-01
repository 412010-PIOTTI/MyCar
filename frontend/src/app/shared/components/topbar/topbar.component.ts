import { Component, DestroyRef, Input, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { interval, of, forkJoin, switchMap, startWith, map, catchError } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { UserService } from '../../../core/services/user.service';
import { VehicleService } from '../../../core/services/vehicle.service';
import { AlertService } from '../../../core/services/alert.service';
import { AlertResponse, UrgencyLevel } from '../../../core/models/alert.model';
import { Vehicle } from '../../../core/models/vehicle.model';

interface NotifItem {
  alert: AlertResponse;
  vehicle: Vehicle;
}

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <header
      class="flex items-center justify-between px-6 bg-white border-b border-surface-border flex-shrink-0"
      style="height: 64px">

      <!-- Page title -->
      <h1 class="text-lg font-semibold text-text-primary">{{ pageTitle }}</h1>

      <!-- Right side -->
      <div class="flex items-center gap-1">

        <!-- ── Backdrop ────────────────────────────────────────────────── -->
        @if (showDropdown) {
          <div class="fixed inset-0 z-40" (click)="closeDropdown()"></div>
        }

        <!-- ── Bell + dropdown ────────────────────────────────────────── -->
        <div class="relative z-50">

          <button
            (click)="toggleDropdown($event)"
            title="Notificaciones"
            class="relative p-2 rounded-md transition-colors"
            [class]="showDropdown
              ? 'text-primary bg-blue-50'
              : 'text-text-secondary hover:text-text-primary hover:bg-gray-50'">

            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
            </svg>

            @if (notifCount > 0) {
              <span class="absolute top-0.5 right-0.5 min-w-[16px] h-4 px-0.5
                           bg-danger rounded-full flex items-center justify-center
                           text-white font-bold leading-none"
                    style="font-size: 9px">
                {{ notifCount > 9 ? '9+' : notifCount }}
              </span>
            }
          </button>

          <!-- Dropdown panel -->
          @if (showDropdown) {
            <div class="absolute right-0 top-full mt-2 w-80 bg-white border border-surface-border
                        rounded-lg shadow-panel overflow-hidden">

              <!-- Header -->
              <div class="flex items-center justify-between px-4 py-3 border-b border-surface-border">
                <p class="text-sm font-semibold text-text-primary">Notificaciones</p>
                @if (notifCount > 0) {
                  <span class="text-[10px] font-semibold bg-red-100 text-danger px-2 py-0.5 rounded-full">
                    {{ notifCount }} activa{{ notifCount !== 1 ? 's' : '' }}
                  </span>
                } @else {
                  <span class="text-xs text-text-muted">Al día</span>
                }
              </div>

              <!-- Items -->
              <div class="max-h-72 overflow-y-auto divide-y divide-surface-border">
                @for (n of notifications; track n.alert.id) {
                  <div class="px-4 py-3 hover:bg-gray-50 transition-colors">
                    <div class="flex items-start gap-3">
                      <span class="mt-1.5 flex-shrink-0 w-2 h-2 rounded-full"
                            [class]="n.alert.urgencyLevel === 'URGENTE' ? 'bg-danger' : 'bg-warning'">
                      </span>
                      <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium text-text-primary truncate">
                          {{ n.alert.title }}
                        </p>
                        <p class="text-xs text-text-muted mt-0.5">
                          {{ n.vehicle.brand }} {{ n.vehicle.model }} · {{ n.vehicle.plate }}
                        </p>
                      </div>
                      <span class="flex-shrink-0 text-[9px] font-semibold uppercase px-1.5 py-0.5 rounded-full"
                            [class]="n.alert.urgencyLevel === 'URGENTE'
                              ? 'bg-red-100 text-danger'
                              : 'bg-amber-100 text-warning'">
                        {{ n.alert.urgencyLevel === 'URGENTE' ? 'Urgente' : 'Aviso' }}
                      </span>
                    </div>
                  </div>
                } @empty {
                  <div class="px-4 py-8 text-center">
                    <p class="text-sm text-text-secondary">Sin alertas activas</p>
                    <p class="text-xs text-text-muted mt-1">Todos tus vehículos están al día</p>
                  </div>
                }
              </div>

              <!-- Footer -->
              <a routerLink="/alerts" (click)="closeDropdown()"
                 class="flex items-center justify-center gap-1 px-4 py-3 text-sm text-primary
                        font-medium border-t border-surface-border hover:bg-blue-50 transition-colors">
                Ver todas las alertas
                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                </svg>
              </a>

            </div>
          }
        </div>

        <!-- ── Help ───────────────────────────────────────────────────── -->
        <button
          title="Ayuda"
          class="p-2 text-text-secondary hover:text-text-primary rounded-md hover:bg-gray-50 transition-colors">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </button>

        <div class="w-px h-6 bg-surface-border mx-2"></div>

        <!-- ── User avatar ─────────────────────────────────────────────── -->
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

  showDropdown = false;
  notifCount   = 0;
  notifications: NotifItem[] = [];

  private destroyRef     = inject(DestroyRef);
  private userService    = inject(UserService);
  private vehicleService = inject(VehicleService);
  private alertService   = inject(AlertService);

  ngOnInit(): void {
    this.userService.getMe()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (user) => {
          this.userName = user.name;
          this.initials = this.buildInitials(user.name);
        },
        error: () => {},
      });

    // Poll every 5 minutes; fires immediately on load
    interval(300_000).pipe(
      startWith(0),
      takeUntilDestroyed(this.destroyRef),
      switchMap(() =>
        this.vehicleService.getVehicles().pipe(
          switchMap(vehicles => {
            if (vehicles.length === 0) return of([] as NotifItem[]);
            return forkJoin(
              vehicles.map(v =>
                this.alertService.getAlerts(v.id).pipe(
                  map(alerts => alerts.map(a => ({ alert: a, vehicle: v })))
                )
              )
            ).pipe(map(results => results.flat()));
          }),
          catchError(() => of([] as NotifItem[]))
        )
      )
    ).subscribe(items => {
      const active = items.filter(
        i => i.alert.urgencyLevel === 'URGENTE' || i.alert.urgencyLevel === 'ADVERTENCIA'
      );
      this.notifCount    = active.length;
      this.notifications = active.slice(0, 8);
    });
  }

  toggleDropdown(event: MouseEvent): void {
    event.stopPropagation();
    this.showDropdown = !this.showDropdown;
  }

  closeDropdown(): void {
    this.showDropdown = false;
  }

  private buildInitials(name: string): string {
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return name.substring(0, 2).toUpperCase();
  }
}
