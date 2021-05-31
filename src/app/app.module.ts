import { NgModule } from '@angular/core'
import { BrowserModule } from '@angular/platform-browser'
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import { RouterModule } from '@angular/router'
import { ServiceWorkerModule } from '@angular/service-worker'
import { AuthModule } from '@auth0/auth0-angular'
import { environment } from 'src/environments/environment'
import { AppRoutingModule } from './app-routing.module'
import { AppComponent } from './app.component'
import { CalendarComponent } from './components/calendar/calendar.component'
import { HomeComponent } from './components/home/home.component'
import { ScholarCommonModule } from './scholar-common/scholar-common.module'

@NgModule({
  declarations: [AppComponent, CalendarComponent, HomeComponent],
  imports: [
    BrowserModule.withServerTransition({ appId: 'serverApp' }),
    BrowserAnimationsModule,
    AppRoutingModule,
    ServiceWorkerModule.register('ngsw-worker.js', { enabled: environment.production }),
    RouterModule,
    ScholarCommonModule,
    AuthModule.forRoot({
      domain: environment.AUTH0_DOMAIN,
      clientId: environment.AUTH0_CLIENT_ID,
      redirectUri: environment.AUTH0_REDIRECT_URI
    })
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}
