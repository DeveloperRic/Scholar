import { NgModule } from '@angular/core'
import { Routes, RouterModule } from '@angular/router'
import { HomeComponent } from './components/home/home.component'
import { LoginCallbackComponent } from './scholar-common/components/login-callback/login-callback.component'

const routes: Routes = [
  { path: '', component: HomeComponent, pathMatch: 'full' },
  { path: 'manage', loadChildren: () => import('./manage/manage.module').then(m => m.ManageModule) },
  { path: 'grades', loadChildren: () => import('./grades/grades.module').then(m => m.GradesModule) },
  { path: 'login/callback', component: LoginCallbackComponent }
  // TODO add a 404 page
]

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
