import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthStore } from '../../core/auth/auth.store';

@Component({
  selector: 'app-home-page',
  imports: [RouterLink],
  template: `
    <main class="home-page">
      <section class="home-card">
        <p class="brand">HOROLOGUE</p>
        <h1>Home tam thoi</h1>
        <p>Dang nhap thanh cong. Trang nay se duoc thay bang dashboard/home that sau.</p>
        <div class="actions">
          <button type="button" (click)="logout()" [disabled]="loggingOut()">
            {{ loggingOut() ? 'Dang logout...' : 'Logout' }}
          </button>
          <a routerLink="/login">Quay lai login</a>
        </div>
      </section>
    </main>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      color: #e4e2e4;
      background: #131315;
      font-family: Inter, Arial, sans-serif;
    }

    .home-page {
      min-height: 100vh;
      display: grid;
      place-items: center;
      padding: 24px;
    }

    .home-card {
      width: min(100%, 560px);
      padding: 36px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 8px;
      background: rgba(255, 255, 255, 0.05);
      text-align: center;
    }

    .brand {
      margin: 0 0 12px;
      color: #e9c176;
      font-weight: 700;
      letter-spacing: 0.08em;
    }

    h1 {
      margin: 0 0 12px;
    }

    p {
      color: #c7c6ca;
    }

    .actions {
      display: flex;
      flex-wrap: wrap;
      justify-content: center;
      gap: 14px;
      align-items: center;
      margin-top: 24px;
    }

    button {
      min-width: 120px;
      height: 42px;
      border: 0;
      border-radius: 6px;
      background: #e9c176;
      color: #151515;
      font-weight: 700;
      cursor: pointer;
    }

    button:disabled {
      cursor: not-allowed;
      opacity: 0.7;
    }

    a {
      color: #e9c176;
      text-decoration: none;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomePage {
  private readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  readonly loggingOut = signal(false);

  logout(): void {
    if (this.loggingOut()) {
      return;
    }

    this.loggingOut.set(true);
    this.auth.logout().subscribe(() => {
      void this.router.navigateByUrl('/login');
    });
  }
}
