import { Routes } from '@angular/router';

import { Login } from './components/auth/login/login';
import { Register } from './components/auth/register/register';
import { Webinars } from './components/webinars/webinars/webinars';
import { authGuard } from './shared/guards/auth-guard';
import { CreateWebinar } from './components/webinars/create-webinar/create-webinar';
import { EditWebinar } from './components/webinars/edit-webinar/edit-webinar';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'webinars', component: Webinars, canActivate: [authGuard] },
  { path: 'webinars/create', component: CreateWebinar, canActivate: [authGuard] },
  { path: 'webinars/:uuid/edit', component: EditWebinar, canActivate: [authGuard] },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' },
];