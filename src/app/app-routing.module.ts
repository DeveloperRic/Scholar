import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CalendarComponent } from './calendar/calendar.component';
import { HomeComponent } from './home/home.component';
import { ManageComponent } from './manage/manage.component';
import { RealmRedirectComponent } from './realm-redirect/realm-redirect.component';

const routes: Routes = [
  { path: '', component: HomeComponent, pathMatch: "full" },
  { path: 'calendar', component: CalendarComponent },
  { path: 'manage', component: ManageComponent },
  { path: 'realm-redirect', component: RealmRedirectComponent }
  // { path: '**', component: E404Component }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
