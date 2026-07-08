import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthStore } from '../../../core/auth/auth.store';
import { googleConfig } from '../../../core/config/google.config';
import { GoogleCredentialResponse } from '../../../shared/models/google-identity.model';

interface LoginForm {
  email: FormControl<string>;
  password: FormControl<string>;
  remember: FormControl<boolean>;
}

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.page.html',
  styleUrl: './login.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  private readonly auth = inject(AuthStore);
  private readonly router = inject(Router);
  protected readonly loading = signal(false);
  protected readonly googleLoading = signal(false);
  protected readonly message = signal('');
  protected readonly isError = signal(false);

  protected readonly form = new FormGroup<LoginForm>({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    remember: new FormControl(false, { nonNullable: true }),
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.message.set('');
    const { email, password } = this.form.getRawValue();

    this.auth.login(email, password).subscribe({
      next: () => {
        this.isError.set(false);
        this.message.set('Đăng nhập thành công.');
        this.loading.set(false);
        void this.router.navigateByUrl('/home');
      },
      error: () => {
        this.isError.set(true);
        this.message.set('Đăng nhập thất bại. Kiểm tra email, mật khẩu hoặc trạng thái verify email.');
        this.loading.set(false);
      },
    });
  }

  protected loginWithGoogle(): void {
    if (!googleConfig.clientId) {
      this.isError.set(true);
      this.message.set('Chua cau hinh Google Client ID.');
      return;
    }

    if (!window.google) {
      this.isError.set(true);
      this.message.set('Google Sign-In chua san sang, vui long thu lai.');
      return;
    }

    this.googleLoading.set(true);
    this.message.set('');
    window.google.accounts.id.initialize({
      client_id: googleConfig.clientId,
      callback: (response) => this.handleGoogleCredential(response),
      auto_select: false,
      cancel_on_tap_outside: true,
    });
    window.google.accounts.id.prompt();
  }

  private handleGoogleCredential(response: GoogleCredentialResponse): void {
    this.auth.loginWithGoogle(response.credential).subscribe({
      next: () => {
        this.isError.set(false);
        this.message.set('Dang nhap Google thanh cong.');
        this.googleLoading.set(false);
        void this.router.navigateByUrl('/home');
      },
      error: () => {
        this.isError.set(true);
        this.message.set('Dang nhap Google that bai.');
        this.googleLoading.set(false);
      },
    });
  }
}
