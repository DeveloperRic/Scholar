import { NgModule } from '@angular/core'
import { DashboardComponent } from './components/dashboard/dashboard.component'
import { ScholarCommonModule } from '../scholar-common/scholar-common.module'
import { GradesRoutingModule } from './grades-routing.module'

@NgModule({
  declarations: [DashboardComponent],
  imports: [GradesRoutingModule, ScholarCommonModule],
  bootstrap: [DashboardComponent]
})
export class GradesModule { }
