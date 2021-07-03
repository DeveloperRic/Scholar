import { Location } from '@angular/common'
import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'
import { Observable } from 'rxjs'
import { RealmService } from 'src/app/database/realm.service'
import { SyncService } from '../../../database/sync.service'

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  private currentPage: string
  isOnline$: Observable<boolean>
  isLoggedIn$: Observable<boolean>

  constructor(
    private router: Router,
    private location: Location,
    private realmService: RealmService
  ) { }

  ngOnInit(): void {
    this.location.onUrlChange(() => {
      this.currentPage = this.location.path().substr(1)
    })
    this.isOnline$ = SyncService.isOnline
    this.isLoggedIn$ = this.realmService.isLoggedIn$
  }

  currentPageIs(pageName: string) {
    if (this.currentPage === undefined) return pageName === 'home'
    if (pageName === 'home') return this.currentPage === ''
    return this.currentPage.startsWith(pageName)
  }

  navigateTo(pageName: string) {
    this.router.navigateByUrl(`/${pageName === 'home' ? '' : pageName}`)
  }
}
