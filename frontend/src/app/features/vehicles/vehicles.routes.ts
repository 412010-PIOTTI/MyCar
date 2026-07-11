import { Routes } from '@angular/router';

export const VEHICLES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./vehicles-list/vehicles-list.component').then((m) => m.VehiclesListComponent),
  },
  {
    path: ':id',
    data: {
      help: 'Vas a encontrar el kilometraje, mantenimientos, gastos, documentos y alertas de este vehículo en particular.',
    },
    loadComponent: () =>
      import('./vehicle-detail/vehicle-detail.component').then((m) => m.VehicleDetailComponent),
  },
];
