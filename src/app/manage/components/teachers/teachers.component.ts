import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { Observable, of } from 'rxjs'
import { switchMap, tap } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Teacher } from 'src/app/model/teacher'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'

@Component({
  selector: 'manage-teachers',
  templateUrl: './teachers.component.html',
  styleUrls: ['../../manage.component.css']
})
export class TeachersComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  teachers$: Observable<Teacher[]>
  hasTeachers: boolean

  constructor(private databaseService: DatabaseService, private popupService: PopupService) { }

  ngOnInit(): void {
    this.teachers$ = this.popupService.runWithPopup(
      'Fetching teachers',
      of(undefined).pipe(
        switchMap(() => this.databaseService.database.all.teachers(this.databaseService.accountId)),
        tap(teachers => this.hasTeachers = teachers.length != 0)
      )
    )
  }

  goToTeacher(teacher?: Teacher): void {
    this.pushViewEvent.emit({
      name: ViewName.TEACHER,
      docId: teacher?._id
    })
  }
}
