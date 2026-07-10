import { ChangeDetectionStrategy, Component, computed, inject, signal, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { AuthStore } from '../../core/auth/auth.store';
import { CheckoutApiService, OrderResponse } from '../../core/api/checkout/checkout-api.service';
import { catchError, map, of, Subscription, interval } from 'rxjs';

interface CheckoutForm {
  email: FormControl<string>;
  news: FormControl<boolean>;
  lastName: FormControl<string>;
  firstName: FormControl<string>;
  company: FormControl<string>;
  address: FormControl<string>;
  apartment: FormControl<string>;
  postalCode: FormControl<string>;
  city: FormControl<string>;
  country: FormControl<string>;
  phone: FormControl<string>;
}

@Component({
  selector: 'app-checkout-page',
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe],
  templateUrl: './checkout.page.html',
  styleUrl: './checkout.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})


export class CheckoutPage implements OnInit {
  private readonly auth = inject(AuthStore);
  private readonly checkoutApi = inject(CheckoutApiService);
  private readonly router = inject(Router);

  // Checkout flow state
  protected readonly currentStep = signal<'info' | 'shipping' | 'payment'>('info');
  protected readonly selectedShippingMethod = signal<'standard' | 'express'>('standard');
  
  // Promo code state
  protected readonly promoCodeInput = signal('');
  protected readonly appliedPromoCode = signal('');
  protected readonly promoError = signal('');
  protected readonly promoSuccess = signal('');
  protected readonly promoDiscount = signal(0); // in VND

  // Order API state
  protected readonly orderLoading = signal(false);
  protected readonly orderError = signal('');
  protected readonly pollingOrder = signal(false);
  protected readonly createdOrder = signal<OrderResponse | null>(null);

  // Subscriptions
  private pollSubscription?: Subscription;

  // Mock checkout items
  protected readonly cartItems = signal([
    {
      id: 1,
      name: 'HOROLOGUE',
      description: 'Bespoke Tourbillon',
      price: 1250000000,
      quantity: 1,
      image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCFx32SJ7wqbdxsoahbIBZeXIeUTCkjPa7EOIfelwF9JOt8hn2h_4qrSK5TUwsnL6llxXIu_jMfq7QjAFbjQOMB1KO8zzYEkHRQDTtm929k6kjfNik1AxvLAHJ8Tli5C7Ov6XiamZNawCErH03WJf13z9j4boHgBkhayBSw2qifrWc-S7xPy0Q3Unncc4C6Sv-5nPy217zAo-nhKlrn5EXo7WvWAdKwGGbiCN8UDJKYhEdD3WKp6yXtMDLXzIODQ8zVLqiuSsTJRbM'
    }
  ]);

  // Checkout form group
  protected readonly form = new FormGroup<CheckoutForm>({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    news: new FormControl(false, { nonNullable: true }),
    lastName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    firstName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    company: new FormControl('', { nonNullable: true }),
    address: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    apartment: new FormControl('', { nonNullable: true }),
    postalCode: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    city: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    country: new FormControl('VN', { nonNullable: true, validators: [Validators.required] }),
    phone: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.pattern(/^[0-9+ ]{9,15}$/)] }),
  });

  // Computed totals
  protected readonly subtotal = computed(() => {
    return this.cartItems().reduce((acc, item) => acc + item.price * item.quantity, 0);
  });

  protected readonly shippingFee = computed(() => {
    if (this.currentStep() === 'info') {
      return 0; // Displayed as "Calculated next" in view
    }
    return this.selectedShippingMethod() === 'express' ? 100000 : 30000;
  });

  protected readonly totalAmount = computed(() => {
    return this.subtotal() + this.shippingFee() - this.promoDiscount();
  });

  ngOnInit(): void {
    // Pre-fill user email if logged in
    const user = this.auth.currentUser();
    if (user && user.email) {
      this.form.patchValue({ email: user.email });
    }
  }

  // Next step handler
  protected nextStep(): void {
    if (this.currentStep() === 'info') {
      if (this.form.invalid) {
        this.form.markAllAsTouched();
        return;
      }
      this.currentStep.set('shipping');
    } else if (this.currentStep() === 'shipping') {
      this.currentStep.set('payment');
    }
  }

  // Previous step handler
  protected prevStep(): void {
    if (this.currentStep() === 'shipping') {
      this.currentStep.set('info');
    } else if (this.currentStep() === 'payment') {
      this.currentStep.set('shipping');
    }
  }

  // Change shipping method
  protected setShippingMethod(method: 'standard' | 'express'): void {
    this.selectedShippingMethod.set(method);
  }

  // Apply discount coupon
  protected applyPromoCode(): void {
    const code = this.promoCodeInput().trim().toUpperCase();
    if (!code) return;

    this.promoError.set('');
    this.promoSuccess.set('');

    if (code === 'HORO10') {
      const discount = Math.round(this.subtotal() * 0.1);
      this.promoDiscount.set(discount);
      this.appliedPromoCode.set(code);
      this.promoSuccess.set('Áp dụng mã giảm giá 10% thành công!');
    } else if (code === 'WELCOME') {
      const discount = Math.round(this.subtotal() * 0.05);
      this.promoDiscount.set(discount);
      this.appliedPromoCode.set(code);
      this.promoSuccess.set('Áp dụng mã giảm giá 5% thành công!');
    } else {
      this.promoDiscount.set(0);
      this.appliedPromoCode.set('');
      this.promoError.set('Mã giảm giá không hợp lệ hoặc đã hết hạn.');
    }
  }

  // Form error helper
  protected hasError(controlName: keyof CheckoutForm, error: string): boolean {
    const control = this.form.controls[controlName];
    return control.hasError(error) && (control.dirty || control.touched);
  }

  // Submit order handler
  protected submitOrder(): void {
    if (this.form.invalid) {
      this.currentStep.set('info');
      this.form.markAllAsTouched();
      return;
    }

    const user = this.auth.currentUser();
    const userId = user ? user.id : 1; // Fallback to 1 if not defined (though guard prevents it)

    const rawValues = this.form.getRawValue();
    // Build clean address string
    const fullAddress = [
      rawValues.address,
      rawValues.apartment,
      rawValues.city,
      rawValues.country === 'VN' ? 'Việt Nam' : rawValues.country
    ].filter(Boolean).join(', ');

    const itemsRequest = this.cartItems().map(item => ({
      productId: item.id,
      quantity: item.quantity,
      unitPrice: item.price
    }));

    this.orderLoading.set(true);
    this.orderError.set('');

    this.checkoutApi.createOrder({
      userId,
      items: itemsRequest,
      shippingAddress: fullAddress
    }).subscribe({
      next: (response) => {
        const order = response.data;
        this.createdOrder.set(order);
        this.orderLoading.set(false);
        this.pollingOrder.set(true);

        // Start polling for payment link
        this.startPolling(order.orderId);
      },
      error: (err) => {
        console.error(err);
        this.orderLoading.set(false);
        this.orderError.set('Đã có lỗi xảy ra khi tạo đơn hàng. Vui lòng thử lại.');
      }
    });
  }

  private startPolling(orderId: number): void {
    // Poll order status every 2 seconds
    this.pollSubscription = interval(2000).subscribe({
      next: () => {
        this.checkoutApi.getOrder(orderId).subscribe({
          next: (response) => {
            const order = response.data;
            this.createdOrder.set(order);
            // If paymentUrl is available, redirect user to PayOS
            if (order.paymentUrl) {
              this.stopPolling();
              window.location.href = order.paymentUrl;
            }
          },
          error: (err) => {
            console.error('Error polling order:', err);
          }
        });
      }
    });

    // Timeout polling after 60 seconds (fall back to error message)
    setTimeout(() => {
      if (this.pollingOrder() && !this.createdOrder()?.paymentUrl) {
        this.stopPolling();
        this.pollingOrder.set(false);
        this.orderError.set('Không nhận được liên kết thanh toán từ máy chủ. Vui lòng kiểm tra lại trạng thái đơn hàng của bạn.');
      }
    }, 60000);
  }

  private stopPolling(): void {
    if (this.pollSubscription) {
      this.pollSubscription.unsubscribe();
      this.pollSubscription = undefined;
    }
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }
}
