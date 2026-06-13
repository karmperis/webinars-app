import { Routes } from '@angular/router';

import { Login } from './components/auth/login/login';
import { Register } from './components/auth/register/register';
import { Webinars } from './components/webinars/webinars/webinars';
import { authGuard } from './shared/guards/auth-guard';
import { CreateWebinar } from './components/webinars/create-webinar/create-webinar';
import { EditWebinar } from './components/webinars/edit-webinar/edit-webinar';
import { MyWebinars } from './components/webinars/my-webinars/my-webinars';
import { OrganizerWebinars } from './components/webinars/organizer-webinars/organizer-webinars';
import { Users } from './components/users/users/users';
import { EditUserAccess } from './components/users/edit-user-access/edit-user-access';
import { EditProfile } from './components/users/edit-profile/edit-profile';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'webinars', component: Webinars, canActivate: [authGuard] },
  { path: 'webinars/create', component: CreateWebinar, canActivate: [authGuard] },
  { path: 'webinars/:uuid/edit', component: EditWebinar, canActivate: [authGuard] },
  { path: 'my-webinars', component: MyWebinars, canActivate: [authGuard] },
  { path: 'organizer-webinars', component: OrganizerWebinars, canActivate: [authGuard] },
  { path: 'users', component: Users, canActivate: [authGuard] },
  { path: 'users/:uuid/access', component: EditUserAccess, canActivate: [authGuard] },
  { path: 'profile', component: EditProfile, canActivate: [authGuard] },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' },
];