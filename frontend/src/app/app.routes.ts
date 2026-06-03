import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { AppShellComponent } from './shared/components/app-shell/app-shell.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full',
  },
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        data: { title: 'Dashboard' },
        loadChildren: () =>
          import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
      },
      {
        path: 'vehicles',
        data: { title: 'Mis vehículos' },
        loadChildren: () =>
          import('./features/vehicles/vehicles.routes').then((m) => m.VEHICLES_ROUTES),
      },
      {
        path: 'expenses',
        data: { title: 'Gastos' },
        loadChildren: () =>
          import('./features/expenses/expenses.routes').then((m) => m.EXPENSES_ROUTES),
      },
      {
        path: 'settings',
        data: { title: 'Configuración' },
        loadChildren: () =>
          import('./features/settings/settings.routes').then((m) => m.SETTINGS_ROUTES),
      },
      {
        path: 'maintenance',
        data: { title: 'Mantenimiento' },
        loadComponent: () =>
          import('./features/maintenance/maintenance.component').then((m) => m.MaintenanceComponent),
      },
      {
        path: 'documents',
        data: { title: 'Documentos' },
        loadComponent: () =>
          import('./features/documents/documents.component').then((m) => m.DocumentsComponent),
      },
      {
        path: 'alerts',
        data: { title: 'Alertas' },
        loadComponent: () =>
          import('./features/alerts/alerts.component').then((m) => m.AlertsComponent),
      },
      {
        path: 'transfers',
        data: { title: 'Transferencias' },
        loadComponent: () =>
          import('./features/transfers/transfers.component').then((m) => m.TransfersComponent),
      },
      {
        path: 'ai-chat',
        data: { title: 'AI Chat' },
        loadComponent: () =>
          import('./features/ai-chat/ai-chat.component').then((m) => m.AiChatComponent),
      },
      {
        path: '**',
        redirectTo: 'dashboard',
      },
    ],
  },
];
