import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthStore } from '../../../core/auth/auth.store';

interface RegisterForm {
  fullName: FormControl<string>;
  username: FormControl<string>;
  email: FormControl<string>;
  phone: FormControl<string>;
  password: FormControl<string>;
  confirmPassword: FormControl<string>;
  terms: FormControl<boolean>;
}

@Component({
  selector: 'app-register-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.page.html',
  styleUrl: './register.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterPage {
  private readonly auth = inject(AuthStore);
  protected readonly loading = signal(false);
  protected readonly message = signal('');
  protected readonly isError = signal(false);

  protected readonly form = new FormGroup<RegisterForm>({
    fullName: new FormControl('', { nonNullable: true }),
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    phone: new FormControl('', { nonNullable: true }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d).{8,}$/)],
    }),
    confirmPassword: new FormControl('', { nonNullable: true }),
    terms: new FormControl(false, { nonNullable: true, validators: [Validators.requiredTrue] }),
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { email, username, password, fullName, phone } = this.form.getRawValue();
    this.loading.set(true);
    this.message.set('');

    this.auth.register({
      email,
      username,
      password,
      fullName: fullName || undefined,
      phone: phone || undefined,
    }).subscribe({
      next: () => {
        this.isError.set(false);
        this.message.set('Đăng ký thành công. Hãy verify email trước khi đăng nhập.');
        this.loading.set(false);
      },
      error: () => {
        this.isError.set(true);
        this.message.set('Đăng ký thất bại. Kiểm tra dữ liệu hoặc email đã tồn tại.');
        this.loading.set(false);
      },
    });
  }
}
