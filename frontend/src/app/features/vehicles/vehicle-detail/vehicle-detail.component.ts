import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';

@Component({
  selector: 'app-vehicle-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './vehicle-detail.component.html',
})
export class VehicleDetailComponent {
  vehicleId: string | null;

  constructor(private route: ActivatedRoute) {
    this.vehicleId = this.route.snapshot.paramMap.get('id');
  }
}
