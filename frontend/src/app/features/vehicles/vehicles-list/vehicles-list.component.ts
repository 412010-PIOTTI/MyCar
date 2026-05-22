import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-vehicles-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './vehicles-list.component.html',
})
export class VehiclesListComponent implements OnInit {
  vehicles: any[] = [];

  ngOnInit(): void {
    // TODO: inyectar VehiclesService y cargar lista
  }
}
