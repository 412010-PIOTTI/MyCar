import { Component, DestroyRef, inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { AuthLayoutComponent } from '../../../shared/components/auth-layout/auth-layout.component';

@Component({
  selector: 'app-verify-two-factor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AuthLayoutComponent],
  templateUrl: './verify-two-factor.component.html',
})
export class VerifyTwoFactorComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  form = this.fb.group({
    code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
  });

  maskedEmail = '';
  loading = false;
  resending = false;
  errorMessage = '';
  successMessage = '';

  resendCooldown = 0;
  private cooldownInterval?: ReturnType<typeof setInterval>;

  ngOnInit(): void {
    if (!this.authService.pending2FAEmail) {
      this.router.navigate(['/auth/login']);
      return;
    }
    this.maskedEmail = this.authService.pending2FAEmail;
  }

  ngOnDestroy(): void {
    if (this.cooldownInterval) clearInterval(this.cooldownInterval);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorMessage = '';

    const code = this.form.value.code!;
    this.authService
      .verify2FA(this.maskedEmail, code)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.loading = false)),
      )
      .subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: (err) => {
          this.errorMessage =
            err.error?.detail ?? 'Código incorrecto. Verificá e intentá de nuevo.';
        },
      });
  }

  resendCode(): void {
    if (this.resendCooldown > 0 || this.resending) return;
    this.resending = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService
      .resend2FA(this.maskedEmail)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.resending = false)),
      )
      .subscribe({
        next: () => {
          this.successMessage = 'Se envió un nuevo código a tu email.';
          this.form.reset();
          this.startCooldown(60);
        },
        error: () => {
          this.errorMessage = 'No se pudo reenviar el código. Intentá de nuevo.';
        },
      });
  }

  cancel(): void {
    this.authService
      .cancel2FA(this.maskedEmail)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ complete: () => this.router.navigate(['/auth/login']) });
  }

  private startCooldown(seconds: number): void {
    this.resendCooldown = seconds;
    this.cooldownInterval = setInterval(() => {
      this.resendCooldown--;
      if (this.resendCooldown <= 0) {
        clearInterval(this.cooldownInterval);
        this.resendCooldown = 0;
      }
    }, 1000);
  }
}
