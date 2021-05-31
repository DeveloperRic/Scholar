import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'

import { AuthService } from '@auth0/auth0-angular'

import { RealmService } from '../../../database/realm.service'

@Component({
  selector: 'app-login-callback',
  templateUrl: './login-callback.component.html',
  styleUrls: ['./login-callback.component.css']
})
export class LoginCallbackComponent implements OnInit {
  constructor(private router: Router, private authService: AuthService, private realmService: RealmService) {}

  ngOnInit(): void {
    console.log('LoginCallbackComponent: Waiting for Auth0 JWT...')
    this.authService.idTokenClaims$.subscribe(async (token: { __raw: string }) => {
      console.log('LoginCallbackButtonComponent: Logging in to Realm...')
      await this.realmService.login(token.__raw)
      console.log('LoginCallbackButtonComponent: Confirming login status...')
      if (!this.realmService.isLoggedIn()) {
        throw new Error('LoginCallbackButtonComponent: Login confirmation failed, realmService.isLoggedIn() returned false')
      }
      console.log('LoginCallbackButtonComponent: Successfully logged in, reloading page...')
      const path = window.location.pathname
      if (path == '/') {
        window.location.reload()
      } else {
        this.router.navigateByUrl(window.location.pathname, { replaceUrl: true })
      }
    })
  }
}
