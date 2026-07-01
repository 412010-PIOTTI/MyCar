import { Component, DestroyRef, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { AuthLayoutComponent } from '../../../shared/components/auth-layout/auth-layout.component';
import { PasswordInputComponent } from '../../../shared/components/password-input/password-input.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, AuthLayoutComponent, PasswordInputComponent],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  loading = false;
  errorMessage = '';

  readonly features = [
    'Documentación siempre al día',
    'Historial de mantenimiento completo',
    'Control de gastos por vehículo',
    'Chat técnico con IA integrada',
  ];

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorMessage = '';

    const { email, password } = this.form.value;
    this.authService
      .login({ email: email!, password: password! })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.loading = false)),
      )
      .subscribe({
        next: (response) => {
          const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
          if (response.requires2FA) {
            this.router.navigate(['/auth/verify-2fa'], returnUrl ? { queryParams: { returnUrl } } : undefined);
          } else {
            this.router.navigateByUrl(returnUrl || '/dashboard');
          }
        },
        error: (err) => {
          this.errorMessage =
            err.status === 401 || err.status === 403
              ? 'Email o contraseña incorrectos.'
              : 'Ocurrió un error. Intentá de nuevo.';
        },
      });
  }
}
