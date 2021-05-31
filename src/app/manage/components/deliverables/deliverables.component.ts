import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'
import { Observable } from 'rxjs'
import { map } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Deliverable } from 'src/app/model/deliverable'
import { PopupService } from 'src/app/services/popup.service'

@Component({
  selector: 'manage-deliverables',
  templateUrl: './deliverables.component.html'
})
export class DeliverablesComponent implements OnInit {
  deliverables$: Observable<Deliverable[]>
  noDeliverables: boolean

  constructor(private router: Router, private databaseService: DatabaseService, private popupService: PopupService) {}

  ngOnInit() {
    this.deliverables$ = this.popupService.runWithPopup(
      'Fetching deliverables',
      this.databaseService.database.all.deliverables(this.databaseService.accountId).pipe(
        map(deliverables => {
          this.noDeliverables = deliverables.length === 0
          return deliverables
        })
      )
    )
  }

  onAddDeliverableClick() {
    // TODO navigateToView(this.router, ViewType.TERM)
  }
}
