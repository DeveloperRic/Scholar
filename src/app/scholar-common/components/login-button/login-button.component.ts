import { Component, Input, OnInit } from '@angular/core'
import { AuthService } from '@auth0/auth0-angular'
import { environment } from 'src/environments/environment'

@Component({
  selector: 'app-login-button',
  templateUrl: './login-button.component.html',
  styles: []
})
export class LoginButtonComponent implements OnInit {
  @Input() returnTo: string

  constructor(private authService: AuthService) { }

  ngOnInit(): void { }

  loginWithRedirect(): void {
    //TODO disallow signup with duplicate email address
    console.log('LoginButtonComponent: Redirecting to Auth0 login...')
    loginToScholar(this.authService, this.returnTo)
  }
}

export function loginToScholar(authService: AuthService, returnTo: string) {
  authService.loginWithRedirect({
    redirect_uri: environment.AUTH0_REDIRECT_URI,
    appState: { target: returnTo }
  })
}
