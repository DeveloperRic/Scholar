import { NgModule } from '@angular/core'
import { Routes, RouterModule } from '@angular/router'
import { IsLoggedInGuard } from '../scholar-common/guards/is-logged-in.guard'
import { DashboardComponent } from './components/dashboard/dashboard.component'

const routes: Routes = [
  {
    path: '',
    canActivate: [IsLoggedInGuard],
    component: DashboardComponent
  }
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GradesRoutingModule { }
