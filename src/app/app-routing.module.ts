import { NgModule } from '@angular/core'
import { Routes, RouterModule } from '@angular/router'

import { AuthGuard } from '@auth0/auth0-angular'

import { CalendarComponent } from './components/calendar/calendar.component'
import { HomeComponent } from './components/home/home.component'
import { LoginCallbackComponent } from './scholar-common/components/login-callback/login-callback.component'

const routes: Routes = [
  { path: '', component: HomeComponent, pathMatch: 'full' },
  { path: 'calendar', component: CalendarComponent, canActivate: [AuthGuard] },
  // TODO AuthGuard connects to Auth0 not Realm so it requires a redirect, code a custom one to prevent redirects
  { path: 'manage', canActivate: [AuthGuard], loadChildren: () => import('./manage/manage.module').then(m => m.ManageModule) },
  { path: 'login/callback', component: LoginCallbackComponent }
  // TODO add a 404 page
]

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
