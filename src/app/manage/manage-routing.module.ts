import { NgModule } from '@angular/core'
import { Routes, RouterModule } from '@angular/router'
import { IsLoggedInGuard } from '../scholar-common/guards/is-logged-in.guard'
import { ManageComponent } from './manage.component'

const routes: Routes = [
  {
    path: '',
    canActivate: [IsLoggedInGuard],
    component: ManageComponent
  }
]

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ManageRoutingModule { }
