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
import { Roles } from './components/roles/roles/roles';
import { CreateRole } from './components/roles/create-role/create-role';
import { EditRole } from './components/roles/edit-role/edit-role';
import { Capabilities } from './components/capabilities/capabilities/capabilities';
import { CreateCapability } from './components/capabilities/create-capability/create-capability';
import { EditCapability } from './components/capabilities/edit-capability/edit-capability';
import { AssignCapability } from './components/roles/assign-capability/assign-capability';
import { Reports } from './components/reports/reports/reports';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'webinars', component: Webinars, canActivate: [authGuard] },
  {
    path: 'webinars/create',
    component: CreateWebinar,
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'ORGANIZER'] },
  },
  { path: 'webinars/:uuid/edit', component: EditWebinar, canActivate: [authGuard] },
  { path: 'my-webinars', component: MyWebinars, canActivate: [authGuard] },
  {
    path: 'organizer-webinars',
    component: OrganizerWebinars,
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'ORGANIZER'] },
  },
  { path: 'users', component: Users, canActivate: [authGuard], data: { roles: ['ADMIN'] } },
  {
    path: 'users/:uuid/access',
    component: EditUserAccess,
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
  },
  { path: 'profile', component: EditProfile, canActivate: [authGuard] },
  { path: 'roles', component: Roles, canActivate: [authGuard], data: { roles: ['ADMIN'] } },
  {
    path: 'roles/create',
    component: CreateRole,
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
  },
  {
    path: 'roles/:uuid/edit',
    component: EditRole,
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
  },
  {
    path: 'capabilities',
    component: Capabilities,
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
  },
  {
    path: 'capabilities/create',
    component: CreateCapability,
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
  },
  {
    path: 'capabilities/:uuid/edit',
    component: EditCapability,
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
  },
  {
    path: 'roles/:uuid/capabilities',
    component: AssignCapability,
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
  },
  { path: 'reports', component: Reports, canActivate: [authGuard], data: { roles: ['ADMIN'] } },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' },
];