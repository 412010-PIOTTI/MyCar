import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AppShellComponent } from '../../shared/components/app-shell/app-shell.component';
import { SectionCardComponent } from '../../shared/components/section-card/section-card.component';
import { ToggleComponent } from '../../shared/components/toggle/toggle.component';
import { UserService } from '../../core/services/user.service';

type FormStatus = 'idle' | 'loading' | 'success' | 'error';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AppShellComponent, SectionCardComponent, ToggleComponent],
  templateUrl: './settings.component.html',
})
export class SettingsComponent implements OnInit {
  private destroyRef = inject(DestroyRef);

  profileForm: FormGroup;
  profileStatus: FormStatus = 'idle';
  profileError = '';

  passwordForm: FormGroup;
  passwordStatus: FormStatus = 'idle';
  passwordError = '';
  showCurrentPassword = false;
  showNewPassword = false;

  userEmail = '';
  userRole = '';

  constructor(private fb: FormBuilder, private userService: UserService) {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
    });
  }

  ngOnInit(): void {
    this.userService
      .getMe()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (user) => {
          this.profileForm.patchValue({ name: user.name });
          this.userEmail = user.email;
          this.userRole = user.role;
        },
      });
  }

  saveProfile(): void {
    if (this.profileForm.invalid) return;
    this.profileStatus = 'loading';
    this.profileError = '';

    this.userService.updateProfile(this.profileForm.value).subscribe({
      next: () => {
        this.profileStatus = 'success';
        setTimeout(() => (this.profileStatus = 'idle'), 3000);
      },
      error: () => {
        this.profileStatus = 'error';
        this.profileError = 'No se pudo actualizar el perfil. Intentá de nuevo.';
      },
    });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) return;
    this.passwordStatus = 'loading';
    this.passwordError = '';

    this.userService.changePassword(this.passwordForm.value).subscribe({
      next: () => {
        this.passwordStatus = 'success';
        this.passwordForm.reset();
        setTimeout(() => (this.passwordStatus = 'idle'), 3000);
      },
      error: (err) => {
        this.passwordStatus = 'error';
        if (err.status === 400) {
          this.passwordError = 'Contraseña actual incorrecta o la nueva no cumple los requisitos.';
        } else {
          this.passwordError = 'No se pudo cambiar la contraseña. Intentá de nuevo.';
        }
      },
    });
  }
}
