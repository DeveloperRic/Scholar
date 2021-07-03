import { NgModule } from '@angular/core'
import { Routes, RouterModule } from '@angular/router'
import { CalendarComponent } from './components/calendar/calendar.component'
import { HomeComponent } from './components/home/home.component'
import { LoginCallbackComponent } from './scholar-common/components/login-callback/login-callback.component'
import { IsLoggedInGuard } from './scholar-common/guards/is-logged-in.guard'

const routes: Routes = [
  { path: '', component: HomeComponent, pathMatch: 'full' },
  { path: 'calendar', component: CalendarComponent, canActivate: [IsLoggedInGuard] },
  { path: 'manage', loadChildren: () => import('./manage/manage.module').then(m => m.ManageModule) },
  { path: 'login/callback', component: LoginCallbackComponent }
  // TODO add a 404 page
]

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
