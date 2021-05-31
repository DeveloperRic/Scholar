import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { Observable } from 'rxjs'
import { map } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Calendar } from 'src/app/model/calendar'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'

@Component({
  selector: 'manage-calendars',
  templateUrl: './calendars.component.html',
  styleUrls: ['../../manage.component.css']
})
export class CalendarsComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  calendars$: Observable<Calendar[]>
  noCalendars: boolean

  constructor(
    private databaseService: DatabaseService,
    private popupService: PopupService
  ) {
  }

  ngOnInit() {
    this.calendars$ = this.popupService.runWithPopup(
      'Fetching calendars',
      this.databaseService.database.all.calendars(this.databaseService.accountId).pipe(
        map(calendars => {
          this.noCalendars = calendars.length === 0
          return calendars
        })
      )
    )
  }

  goToCalendar(calendar?: Calendar) {
    this.pushViewEvent.emit({ name: ViewName.CALENDAR, docId: calendar?._id })
  }

}
