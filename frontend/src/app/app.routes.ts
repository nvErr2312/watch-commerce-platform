import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.page').then((m) => m.RegisterPage),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.page').then((m) => m.LoginPage),
  },
  {
    path: 'home',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/home/home.page').then((m) => m.HomePage),
  },
  {
    // TODO: swap authGuard for an admin-role guard once role claims are
    // exposed on the token - right now any logged-in user can reach this.
    path: 'admin/products',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/admin/products/product-management.page').then((m) => m.ProductManagementPage),
  },
];
