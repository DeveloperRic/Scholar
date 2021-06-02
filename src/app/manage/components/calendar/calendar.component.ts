import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { FormControl, FormGroup, Validators } from '@angular/forms'
import { Observable, of } from 'rxjs'
import { map, switchMap, take } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { PopupService } from 'src/app/services/popup.service'
import { Calendar } from 'src/app/model/calendar'
import { Term } from 'src/app/model/term'
import { ObjectId } from 'bson'
import { ErrorCodes } from 'src/app/services/ErrorCodes'
import { Teacher } from 'src/app/model/teacher'
import { ViewInfo, ViewName } from '../../manage.component'

@Component({
  selector: 'manage-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['../../manage.component.css']
})
export class CalendarComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  @Output() popViewEvent = new EventEmitter<void>()
  calendarId$: Observable<Calendar['_id']>
  calendar$: Observable<Calendar>
  terms$: Observable<Term[]>
  teachers$: Observable<Teacher[]>
  hasTerms: boolean
  hasTeachers: boolean
  form: FormGroup

  constructor(private activatedRoute: ActivatedRoute, private databaseService: DatabaseService, private popupService: PopupService) { }

  ngOnInit() {
    this.calendarId$ = this.activatedRoute.queryParamMap.pipe(map(queryParams => queryParams.get('docId')))
    this.calendar$ = this.calendarId$.pipe(
      switchMap(calendarId => {
        if (!calendarId) return of(null)
        return this.popupService.runWithPopup('Fetching calendar', this.databaseService.database.fetch.calendar(calendarId))
      }),
      map(calendar => {
        this.setForm(calendar)
        return calendar
      })
    )
    this.terms$ = this.calendarId$.pipe(
      switchMap(calendarId => {
        if (!calendarId) return of([])
        return this.databaseService.database.all.terms(calendarId)
      }),
      map(terms => {
        this.hasTerms = terms.length != 0
        return terms
      })
    )
    this.teachers$ = this.calendarId$.pipe(
      switchMap(calendarId => {
        if (!calendarId) return of([])
        return this.databaseService.database.all.teachers(calendarId)
      }),
      map(teachers => {
        this.hasTeachers = teachers.length != 0
        return teachers
      })
    )
  }

  setForm(calendar?: Calendar) {
    const currentYear = new Date().getFullYear()
    const initialState = calendar?.year ?? currentYear
    this.form = new FormGroup({
      year: new FormControl(initialState, [Validators.required, Validators.min(currentYear - 1)])
    })
  }

  async submit() {
    await this.popupService
      .runWithPopup(
        'Saving calendar',
        this.calendarId$.pipe(
          take(1),
          switchMap(calendarId => {
            const calendar: Calendar = {
              _id: calendarId || new ObjectId().toHexString(),
              account: this.databaseService.accountId,
              year: this.form.get('year').value
            }
            return this.databaseService.database.put.calendar(calendar).pipe(map(() => calendar._id))
          }),
          map(docId => {
            this.pushViewEvent.emit({
              name: ViewName.CALENDAR,
              docId,
              replacesUrl: true
            })
          })
        ),
        ErrorCodes.ERR_CALENDAR_EXISTS
      )
      .toPromise()
  }

  async removeCalendar(calendar: Calendar) {
    if (!confirm('Are you sure you want to remove this calendar?')) return
    await this.popupService
      .runWithPopup('Removing calendar', this.databaseService.database.remove.calendar(calendar._id).pipe(map(() => this.popViewEvent.emit())))
      .toPromise()
  }

  async goToTerm(term?: Term) {
    await this.calendarId$.pipe(
      take(1),
      map(calendarId => this.pushViewEvent.emit({
        name: ViewName.TERM,
        docId: term?._id,
        parentId: calendarId
      }))
    ).toPromise()
  }
}
