import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'
import { Observable } from 'rxjs'
import { map } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Test } from 'src/app/model/test'
import { PopupService } from 'src/app/services/popup.service'

@Component({
  selector: 'manage-tests',
  templateUrl: './tests.component.html'
})
export class TestsComponent implements OnInit {
  tests$: Observable<Test[]>
  noTests: boolean

  constructor(private router: Router, private databaseService: DatabaseService, private popupService: PopupService) {}

  ngOnInit() {
    this.tests$ = this.popupService.runWithPopup(
      'Fetching tests',
      this.databaseService.database.all.tests(this.databaseService.accountId).pipe(
        map(tests => {
          this.noTests = tests.length === 0
          return tests
        })
      )
    )
  }

  onAddTestClick() {
    // TODO navigateToView(this.router, ViewType.TERM)
  }
}
