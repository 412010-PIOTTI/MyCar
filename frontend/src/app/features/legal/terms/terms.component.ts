import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Location } from '@angular/common';

@Component({
  selector: 'app-terms',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './terms.component.html',
})
export class TermsComponent {
  constructor(private location: Location) {}

  goBack(): void {
    // Falls back to the dashboard when this page was opened directly (e.g. a new tab), where
    // there's no previous entry in this tab's own history to go back to.
    if (window.history.length > 1) {
      this.location.back();
    }
  }
}
