import { Location } from '@angular/common';
import { Component, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { SyncService } from '../../../database/sync.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  private currentPage: string
  @Output() isOnline: boolean

  constructor(
    private router: Router,
    private location: Location
  ) { }

  ngOnInit(): void {
    this.location.onUrlChange(() => {
      this.currentPage = this.location.path().substr(1)
    })
    SyncService.isOnline.subscribe(isOnline => this.isOnline = isOnline)
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
