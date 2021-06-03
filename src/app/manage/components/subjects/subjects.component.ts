import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { Observable } from 'rxjs'
import { map } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Subject } from 'src/app/model/subject'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'

@Component({
  selector: 'manage-subjects',
  templateUrl: './subjects.component.html',
  styleUrls: ['../../manage.component.css']
})
export class SubjectsComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  subjects$: Observable<Subject[]>
  noSubjects: boolean

  constructor(private databaseService: DatabaseService, private popupService: PopupService) { }

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

  goToSubject(subject?: Subject) {
    this.pushViewEvent.emit({ name: ViewName.SUBJECT, docId: subject?._id })
  }
}
