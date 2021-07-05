import { Injectable } from '@angular/core'
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router'
import { AuthService } from '@auth0/auth0-angular'
import { Observable, zip } from 'rxjs'
import { map, take } from 'rxjs/operators'
import { RealmService } from 'src/app/database/realm.service'
import { loginToScholar } from '../components/login-button/login-button.component'

@Injectable({
  providedIn: 'root'
})
export class IsLoggedInGuard implements CanActivate {

  constructor(
    private realmService: RealmService,
    private authService: AuthService
  ) { }

  canActivate(
    _route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return ensureLoggedIn(this.authService, this.realmService, state.url)
  }

}

export function ensureLoggedIn(authService: AuthService, realmService: RealmService, returnTo: string): Observable<boolean> {
  return zip(authService.isAuthenticated$, realmService.isLoggedIn$).pipe(
    take(1),
    map(([isLoggedIntoAuth0, isLoggedIntoRealm]) => {
      const isLoggedIn = isLoggedIntoAuth0 && isLoggedIntoRealm
      if (!isLoggedIn) {
        loginToScholar(authService, returnTo) // performs a redirect
      }
      return isLoggedIn
    })
  )
}
