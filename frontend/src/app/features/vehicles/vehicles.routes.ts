import { Routes } from '@angular/router';

export const VEHICLES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./vehicles-list/vehicles-list.component').then((m) => m.VehiclesListComponent),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./vehicle-detail/vehicle-detail.component').then((m) => m.VehicleDetailComponent),
  },
];
