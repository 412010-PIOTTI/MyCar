import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SectionCardComponent } from '../../shared/components/section-card/section-card.component';
import { ToggleComponent } from '../../shared/components/toggle/toggle.component';
import { ConfirmDeleteModalComponent } from '../../shared/components/confirm-delete-modal/confirm-delete-modal.component';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';

type FormStatus = 'idle' | 'loading' | 'success' | 'error';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SectionCardComponent, ToggleComponent, ConfirmDeleteModalComponent],
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

  // 2FA state
  twoFactorEnabled = false;
  show2FAModal = false;
  twoFAModalPasswordControl = new FormControl<string | null>('', Validators.required);
  twoFAModalLoading = false;
  twoFAModalError = '';
  pending2FAEnabled = false;

  showDeleteModal = false;
  deleteLoading = false;
  deleteError = '';

  constructor(private fb: FormBuilder, private userService: UserService, private authService: AuthService) {
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
          this.twoFactorEnabled = user.twoFactorEnabled;
        },
      });
  }

  onToggle2FA(newValue: boolean): void {
    this.pending2FAEnabled = newValue;
    this.twoFAModalPasswordControl.reset('');
    this.twoFAModalError = '';
    this.show2FAModal = true;
  }

  close2FAModal(): void {
    if (this.twoFAModalLoading) return;
    this.show2FAModal = false;
    this.twoFAModalError = '';
  }

  confirm2FAToggle(): void {
    this.twoFAModalPasswordControl.markAsTouched();
    if (this.twoFAModalPasswordControl.invalid) return;

    this.twoFAModalLoading = true;
    this.twoFAModalError = '';

    this.userService
      .toggle2FA(this.pending2FAEnabled, this.twoFAModalPasswordControl.value!)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (user) => {
          this.twoFactorEnabled = user.twoFactorEnabled;
          this.twoFAModalLoading = false;
          this.show2FAModal = false;
        },
        error: (err) => {
          this.twoFAModalLoading = false;
          if (err.status === 400) {
            this.twoFAModalError = 'Contraseña incorrecta. Verificá e intentá de nuevo.';
          } else {
            this.twoFAModalError = 'No se pudo actualizar la configuración. Intentá de nuevo.';
          }
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

  openDeleteModal(): void {
    this.deleteError = '';
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.deleteError = '';
  }

  onDeleteConfirmed(password: string): void {
    this.deleteLoading = true;
    this.deleteError = '';

    this.userService
      .deleteAccount(password)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.deleteLoading = false;
          this.authService.clearLocalSession();
        },
        error: (err) => {
          this.deleteLoading = false;
          if (err.status === 400) {
            this.deleteError = 'Contraseña incorrecta. Verificá e intentá de nuevo.';
          } else {
            this.deleteError = 'No se pudo procesar la solicitud. Intentá de nuevo.';
          }
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
