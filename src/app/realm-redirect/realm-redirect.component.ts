import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RealmService } from '../database/realm.service';

@Component({
  selector: 'app-realm-redirect',
  templateUrl: './realm-redirect.component.html',
  styleUrls: ['./realm-redirect.component.css']
})
export class RealmRedirectComponent implements OnInit {

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private realmService: RealmService
  ) { }

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      console.log(params)
      // this.realmService.login()
    })
  }

}
