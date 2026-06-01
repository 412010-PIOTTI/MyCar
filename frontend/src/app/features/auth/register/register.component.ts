import { Component, DestroyRef, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { AuthLayoutComponent } from '../../../shared/components/auth-layout/auth-layout.component';
import { PasswordInputComponent } from '../../../shared/components/password-input/password-input.component';

function passwordsMatch(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirm = control.get('confirmPassword')?.value;
  return password && confirm && password !== confirm ? { passwordsMismatch: true } : null;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, AuthLayoutComponent, PasswordInputComponent],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  form = this.fb.group(
    {
      firstName: ['', [Validators.required, Validators.maxLength(50)]],
      lastName: ['', [Validators.required, Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
      role: ['USER', Validators.required],
      terms: [false, Validators.requiredTrue],
    },
    { validators: passwordsMatch },
  );

  loading = false;
  errorMessage = '';

  readonly stats = [
    { value: '12k+', label: 'Usuarios activos' },
    { value: '98%', label: 'Satisfacción' },
    { value: '0$', label: 'Para siempre gratis' },
  ];

  readonly benefits = [
    'Sin tarjeta de crédito requerida',
    'Configuración en menos de 2 minutos',
    'Soporte técnico incluido',
    'Exportación de datos en cualquier momento',
  ];

  selectRole(role: string): void {
    this.form.patchValue({ role });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorMessage = '';

    const { firstName, lastName, email, password } = this.form.value;
    const name = `${firstName!.trim()} ${lastName!.trim()}`;

    this.authService
      .register({ name, email: email!, password: password! })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.loading = false)),
      )
      .subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: (err) => {
          this.errorMessage =
            err.status === 409
              ? 'El email ya está registrado.'
              : 'Ocurrió un error. Intentá de nuevo.';
        },
      });
  }
}
