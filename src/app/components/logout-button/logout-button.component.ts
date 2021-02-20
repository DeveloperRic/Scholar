import { Component, OnInit } from '@angular/core';

import { AuthService } from '@auth0/auth0-angular';

import { RealmService } from '../../database/realm.service';
import { environment } from '../../../environments/environment'

@Component({
  selector: 'app-logout-button',
  templateUrl: './logout-button.component.html',
  styles: [],
})
export class LogoutButtonComponent implements OnInit {
  constructor(
    private authService: AuthService,
    private realmService: RealmService
  ) { }

  ngOnInit(): void { }

  async logout() {
    console.log('LogoutButtonComponent: Logging out...')
    await this.realmService.logout()
    console.log('LogoutButtonComponent: Redirecting to Auth0 logout...')
    this.authService.logout({ returnTo: environment.AUTH0_LOGOUT_URI });
  }
}
