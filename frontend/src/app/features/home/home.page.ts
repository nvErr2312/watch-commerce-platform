import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthStore } from '../../core/auth/auth.store';
import { CartService } from '../../core/cart/cart.service';

interface Product {
  id: string;
  brand: string;
  name: string;
  description: string;
  price: number;
  image: string;
}

@Component({
  selector: 'app-home-page',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './home.page.html',
  styleUrl: './home.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomePage {
  private readonly auth = inject(AuthStore);
  private readonly cart = inject(CartService);
  private readonly router = inject(Router);

  protected readonly loggingOut = signal(false);
  protected readonly userMenuOpen = signal(false);
  protected readonly cartDrawerOpen = signal(false);
  protected readonly toastMessage = signal('');
  protected readonly isAdmin = this.auth.isAdmin;
  protected readonly cartItems = this.cart.items;
  protected readonly cartCount = this.cart.totalItemsCount;
  protected readonly cartSubtotal = this.cart.subtotal;
  protected readonly products: Product[] = [
    {
      id: '00000000-0000-0000-0000-000000000001',
      brand: 'HOROLOGUE',
      name: 'Bespoke Tourbillon',
      description: 'Dong ho co cao cap',
      price: 1000,
      image:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuCFx32SJ7wqbdxsoahbIBZeXIeUTCkjPa7EOIfelwF9JOt8hn2h_4qrSK5TUwsnL6llxXIu_jMfq7QjAFbjQOMB1KO8zzYEkHRQDTtm929k6kjfNik1AxvLAHJ8Tli5C7Ov6XiamZNawCErH03WJf13z9j4boHgBkhayBSw2qifrWc-S7xPy0Q3Unncc4C6Sv-5nPy217zAo-nhKlrn5EXo7WvWAdKwGGbiCN8UDJKYhEdD3WKp6yXtMDLXzIODQ8zVLqiuSsTJRbM',
    },
  ];

  protected toggleUserMenu(): void {
    this.userMenuOpen.update((open) => !open);
  }

  protected closeUserMenu(): void {
    this.userMenuOpen.set(false);
  }

  protected toggleCartDrawer(): void {
    this.cartDrawerOpen.update((open) => !open);
  }

  protected closeCartDrawer(): void {
    this.cartDrawerOpen.set(false);
  }

  protected addToCart(product: Product): void {
    this.cart.addToCart(product);
    this.toastMessage.set('Đã thêm vào giỏ hàng');
    setTimeout(() => this.toastMessage.set(''), 1600);
  }

  protected buyNow(product: Product): void {
    this.addToCart(product);
    void this.router.navigateByUrl('/checkout');
  }

  protected updateQuantity(id: string, quantity: number): void {
    this.cart.updateQuantity(id, quantity);
  }

  protected removeFromCart(id: string): void {
    this.cart.removeFromCart(id);
  }

  protected logout(): void {
    if (this.loggingOut()) {
      return;
    }

    this.loggingOut.set(true);
    this.auth.logout().subscribe(() => {
      void this.router.navigateByUrl('/login');
    });
  }
}
