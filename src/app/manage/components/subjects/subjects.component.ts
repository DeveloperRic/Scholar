import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'
import { Observable } from 'rxjs'
import { map } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Subject } from 'src/app/model/subject'
import { PopupService } from 'src/app/services/popup.service'

@Component({
  selector: 'manage-subjects',
  templateUrl: './subjects.component.html'
})
export class SubjectsComponent implements OnInit {
  subjects$: Observable<Subject[]>
  noSubjects: boolean

  constructor(private router: Router, private databaseService: DatabaseService, private popupService: PopupService) {}

  ngOnInit() {
    this.subjects$ = this.popupService.runWithPopup(
      'Fetching subjects',
      this.databaseService.database.all.subjects(this.databaseService.accountId).pipe(
        map(subjects => {
          this.noSubjects = subjects.length === 0
          return subjects
        })
      )
    )
  }

  onAddSubjectClick() {
    // TODO navigateToView(this.router, ViewType.TERM)
  }
}
