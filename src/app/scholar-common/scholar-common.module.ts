import { CommonModule } from '@angular/common'
import { NgModule } from '@angular/core'
import { ReactiveFormsModule } from '@angular/forms'
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner'
import { LoginButtonComponent } from './components/login-button/login-button.component'
import { LoginCallbackComponent } from './components/login-callback/login-callback.component'
import { LogoutButtonComponent } from './components/logout-button/logout-button.component'
import { NavbarComponent } from './components/navbar/navbar.component'
import { PopupComponent } from './components/popup/popup.component'
import { VarDirective } from './ng-var.directive'

@NgModule({
  declarations: [
    LoginButtonComponent,
    LoginCallbackComponent,
    LogoutButtonComponent,
    NavbarComponent,
    PopupComponent,
    VarDirective
    // Don't forget to export new declarations
  ],
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule
  ],
  exports: [
    LoginButtonComponent,
    LoginCallbackComponent,
    LogoutButtonComponent,
    NavbarComponent,
    PopupComponent,
    VarDirective,
    CommonModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule
  ]
})
export class ScholarCommonModule { }
