import { Component, OnInit } from '@angular/core'
import { AuthService } from '@auth0/auth0-angular'

@Component({
  selector: 'app-login-button',
  templateUrl: './login-button.component.html',
  styles: []
})
export class LoginButtonComponent implements OnInit {
  constructor(private authService: AuthService) {}

  ngOnInit(): void {}

  loginWithRedirect(): void {
    //TODO disallow signup with duplicate email address
    console.log('LoginButtonComponent: Redirecting to Auth0 login...')
    this.authService.loginWithRedirect()
  }
}
