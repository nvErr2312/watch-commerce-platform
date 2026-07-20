import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { AdminCustomer, CustomersApiService } from '../../../core/api/admin/customers-api.service';

@Component({
  selector: 'app-customer-management-page',
  imports: [DecimalPipe],
  templateUrl: './customer-management.page.html',
  styleUrl: './customer-management.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerManagementPage {
  private readonly api = inject(CustomersApiService);
  protected readonly customers = signal<AdminCustomer[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly search = signal('');
  protected readonly status = signal('');
  protected readonly deletingId = signal<string | number | null>(null);
  protected readonly filtered = computed(() => {
    const term = this.search().trim().toLowerCase();
    const selectedStatus = this.status();
    return this.customers().filter((customer) =>
      (!term || [customer.fullName, customer.email, customer.username, customer.phone].some((value) => value?.toLowerCase().includes(term)))
      && (!selectedStatus || customer.status === selectedStatus)
    );
  });

  constructor() {
    this.reload();
  }

  protected reload(): void {
    this.loading.set(true);
    this.api.list(this.search(), this.status()).subscribe({
      next: (response) => {
        this.customers.set(response.data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Khong tai duoc danh sach khach hang.');
        this.loading.set(false);
      },
    });
  }

  protected setSearch(value: string): void {
    this.search.set(value);
    this.reload();
  }

  protected setStatus(value: string): void {
    this.status.set(value);
    this.reload();
  }

  protected isProtected(customer: AdminCustomer): boolean {
    return String(customer.id) === '1';
  }

  protected deleteCustomer(customer: AdminCustomer): void {
    if (this.isProtected(customer) || !window.confirm(`Xoa tai khoan ${customer.email}?`)) return;
    this.deletingId.set(customer.id);
    this.api.delete(customer.id).subscribe({
      next: () => {
        this.deletingId.set(null);
        this.reload();
      },
      error: () => {
        this.deletingId.set(null);
        this.error.set('Khong xoa duoc tai khoan khach hang.');
      },
    });
  }
}
