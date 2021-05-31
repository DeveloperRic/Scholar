import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'
import { Observable } from 'rxjs'
import { map } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Class } from 'src/app/model/class'
import { PopupService } from 'src/app/services/popup.service'

@Component({
  selector: 'manage-classes',
  templateUrl: './classes.component.html'
})
export class ClassesComponent implements OnInit {
  classes$: Observable<Class[]>
  noClasses: boolean

  constructor(private router: Router, private databaseService: DatabaseService, private popupService: PopupService) {}

  ngOnInit() {
    this.classes$ = this.popupService.runWithPopup(
      'Fetching classes',
      this.databaseService.database.all.classes(this.databaseService.accountId).pipe(
        map(classes => {
          this.noClasses = classes.length === 0
          return classes
        })
      )
    )
  }

  onAddClassClick() {
    // TODO navigateToView(this.router, ViewType.TERM)
  }
}
