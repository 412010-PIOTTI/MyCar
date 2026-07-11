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
    path: 'transfer/confirm',
    loadComponent: () =>
      import('./features/transfers/transfer-confirm/transfer-confirm.component').then(
        (m) => m.TransferConfirmComponent,
      ),
  },
  {
    path: 'legal/terminos',
    loadComponent: () =>
      import('./features/legal/terms/terms.component').then((m) => m.TermsComponent),
  },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        data: {
          title: 'Dashboard',
          help: 'Acá tenés un resumen general: tus vehículos, próximos vencimientos y la actividad más reciente.',
        },
        loadChildren: () =>
          import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
      },
      {
        path: 'vehicles',
        data: {
          title: 'Mis vehículos',
          help: 'Administrá tus vehículos: agregá uno nuevo, editá sus datos o entrá al detalle de cada uno.',
        },
        loadChildren: () =>
          import('./features/vehicles/vehicles.routes').then((m) => m.VEHICLES_ROUTES),
      },
      {
        path: 'expenses',
        data: {
          title: 'Gastos',
          help: 'Registrá y consultá los gastos de tus vehículos, agrupados por categoría.',
        },
        loadChildren: () =>
          import('./features/expenses/expenses.routes').then((m) => m.EXPENSES_ROUTES),
      },
      {
        path: 'faq',
        data: {
          title: 'Preguntas Frecuentes',
          help: 'Respuestas a las dudas más comunes sobre cómo usar MyCar.',
        },
        loadComponent: () =>
          import('./features/faq/faq.component').then((m) => m.FaqComponent),
      },
      {
        path: 'settings',
        data: {
          title: 'Configuración',
          help: 'Actualizá los datos de tu cuenta, cambiá tu contraseña o configurá otras preferencias.',
        },
        loadChildren: () =>
          import('./features/settings/settings.routes').then((m) => m.SETTINGS_ROUTES),
      },
      {
        path: 'maintenance',
        data: {
          title: 'Mantenimiento',
          help: 'Llevá el historial de mantenimientos: qué se hizo, en qué sistema y con qué kilometraje.',
        },
        loadComponent: () =>
          import('./features/maintenance/maintenance.component').then((m) => m.MaintenanceComponent),
      },
      {
        path: 'documents',
        data: {
          title: 'Documentos',
          help: 'Guardá y organizá los documentos de tus vehículos, como el seguro, la VTV o la cédula.',
        },
        loadComponent: () =>
          import('./features/documents/documents.component').then((m) => m.DocumentsComponent),
      },
      {
        path: 'alerts',
        data: {
          title: 'Alertas',
          help: 'Revisá las alertas activas de tus vehículos: vencimientos próximos o urgentes.',
        },
        loadComponent: () =>
          import('./features/alerts/alerts.component').then((m) => m.AlertsComponent),
      },
      {
        path: 'transfers',
        data: {
          title: 'Transferencias',
          help: 'Transferí la propiedad de un vehículo a otra persona generando un token de transferencia.',
        },
        loadComponent: () =>
          import('./features/transfers/transfers.component').then((m) => m.TransfersComponent),
      },
      {
        path: 'ai-chat',
        data: {
          title: 'AI Chat',
          help: 'Charlá con el asistente para resolver dudas sobre tus vehículos y su mantenimiento.',
        },
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
