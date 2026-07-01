import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { TransferService } from '../../../core/services/transfer.service';
import { TransferPreviewResponse } from '../../../core/models/transfer.model';

type ViewState = 'loading' | 'preview' | 'not-found' | 'invalid' | 'confirmed' | 'error';

@Component({
  selector: 'app-transfer-confirm',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './transfer-confirm.component.html',
})
export class TransferConfirmComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private transferService = inject(TransferService);
  private destroyRef = inject(DestroyRef);

  token = '';
  state: ViewState = 'loading';
  invalidMessage = '';
  preview: TransferPreviewResponse | null = null;

  confirming = false;
  confirmError = '';
  confirmedVehiclePlate = '';

  get isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.state = 'not-found';
      return;
    }
    this.loadPreview();
  }

  loadPreview(): void {
    this.state = 'loading';
    this.transferService
      .preview(this.token)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (preview) => {
          this.preview = preview;
          this.state = 'preview';
        },
        error: (err) => {
          if (err.status === 404) {
            this.state = 'not-found';
          } else if (err.status === 410) {
            this.invalidMessage = err.error?.detail ?? 'Este código de transferencia ya no es válido.';
            this.state = 'invalid';
          } else {
            this.state = 'error';
          }
        },
      });
  }

  goToLogin(): void {
    this.router.navigate(['/auth/login'], {
      queryParams: { returnUrl: `/transfer/confirm?token=${this.token}` },
    });
  }

  confirmTransfer(): void {
    if (!this.isAuthenticated) {
      this.goToLogin();
      return;
    }
    this.confirming = true;
    this.confirmError = '';
    this.transferService
      .confirm(this.token)
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => (this.confirming = false)))
      .subscribe({
        next: (result) => {
          this.confirmedVehiclePlate = result.vehiclePlate;
          this.state = 'confirmed';
        },
        error: (err) => {
          this.confirmError = err.error?.detail ?? 'No se pudo confirmar la transferencia. Intentá de nuevo.';
        },
      });
  }

  formatDate(dateStr: string | null): string {
    if (!dateStr) return '—';
    const [year, month, day] = dateStr.split('-');
    const months = ['ENE', 'FEB', 'MAR', 'ABR', 'MAY', 'JUN', 'JUL', 'AGO', 'SEP', 'OCT', 'NOV', 'DIC'];
    return `${parseInt(day, 10)} ${months[parseInt(month, 10) - 1]} ${year}`;
  }

  hoursRemaining(expiresAt: string): number {
    return Math.max(0, Math.round((new Date(expiresAt).getTime() - Date.now()) / 3_600_000));
  }
}
