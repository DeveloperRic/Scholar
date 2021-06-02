import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { FormGroup, FormControl, Validators } from '@angular/forms'
import { ActivatedRoute } from '@angular/router'
import { ObjectId } from 'bson'
import { Observable, of, zip } from 'rxjs'
import { filter, map, switchMap, take } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Calendar } from 'src/app/model/calendar'
import { Course } from 'src/app/model/course'
import { Term } from 'src/app/model/term'
import { ErrorCodes } from 'src/app/services/ErrorCodes'
import { PopupService } from 'src/app/services/popup.service'
import { UtilService } from 'src/app/services/util.service'
import { ViewInfo, TITLE_REGEX, ViewName } from '../../manage.component'

@Component({
  selector: 'manage-term',
  templateUrl: './term.component.html'
})
export class TermComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  @Output() popViewEvent = new EventEmitter<void>()
  termId$: Observable<Term['_id']>
  calendarId$: Observable<Calendar['_id']>
  term$: Observable<Term>
  calendar$: Observable<Calendar>
  courses$: Observable<Course[]>
  hasCourses: boolean
  form: FormGroup

  constructor(
    private activatedRoute: ActivatedRoute,
    private databaseService: DatabaseService,
    private popupService: PopupService,
    private util: UtilService
  ) { }

  ngOnInit() {
    this.termId$ = this.activatedRoute.queryParamMap.pipe(map(queryParams => queryParams.get('docId')))
    this.calendarId$ = this.activatedRoute.queryParamMap.pipe(
      map(queryParams => queryParams.get('parentId')),
      filter(calendarId => !!calendarId)
    )
    this.term$ = this.termId$.pipe(
      switchMap(termId => {
        if (!termId) return of(null)
        return this.popupService.runWithPopup('Fetching term', this.databaseService.database.fetch.term(termId))
      }),
      switchMap(term => this.setForm(term))
    )
    this.calendar$ = this.calendarId$.pipe(
      switchMap(calendarId => this.databaseService.database.fetch.calendar(calendarId).pipe(
        map(calendar => {
          if (!calendar) {
            this.popViewEvent.emit()
            this.popupService.newPopup({
              type: 'error',
              message: `Detected invalid calendarId ${calendarId}`
            })
            return
          }
          return calendar
        }),
        filter(calendar => !!calendar)
      )))
    this.courses$ = this.termId$.pipe(
      switchMap(termId => {
        if (!termId) return of([])
        return this.databaseService.database.all.courses(termId)
      }),
      map(courses => {
        this.hasCourses = courses.length != 0
        return courses
      })
    )
  }

  setForm(term?: Term): Observable<Term> {
    return this.calendar$.pipe(
      map(calendar => {
        const currentMonth = new Date().getMonth()
        const autoTermName = currentMonth >= 8 && currentMonth <= 11 ? 'Fall' : currentMonth >= 0 && currentMonth <= 3 ? 'Winter' : 'Summer'
        const autoStartDate = new Date(calendar.year, currentMonth, 1)
        const autoEndDate = new Date(calendar.year, currentMonth + 1, 0)
        const initialState = {
          name: term ? term.name : autoTermName,
          start: this.util.toHTMLDate(term ? term.start || autoStartDate : autoStartDate),
          end: this.util.toHTMLDate(term ? term.end || autoEndDate : autoEndDate)
        }
        const minDate = new Date(calendar.year, 0)
        const maxDate = new Date(calendar.year + 1, 11, 31)
        this.form = new FormGroup({
          name: new FormControl(initialState.name, [Validators.required, Validators.pattern(TITLE_REGEX)]),
          start: new FormControl(initialState.start, [Validators.required, this.util.getDateValidator(minDate, maxDate)]),
          end: new FormControl(initialState.end, [Validators.required, this.util.getDateValidator(minDate, maxDate)])
        })
        return term
      })
    )
  }

  async submit() {
    await this.popupService.runWithPopup(
      'Saving term',
      zip(this.termId$, this.calendarId$).pipe(
        take(1),
        switchMap(([termId, calendarId]) => {
          const term: Term = {
            _id: termId || new ObjectId().toHexString(),
            account: this.databaseService.accountId,
            calendar: calendarId,
            name: this.form.get('name').value,
            start: this.util.fromHTMLDate(this.form.get('start').value).getTime(),
            end: this.util.fromHTMLDate(this.form.get('end').value).getTime()
          }
          return this.databaseService.database.put.term(term).pipe(
            map(() => this.pushViewEvent.emit({
              name: ViewName.TERM,
              docId: term._id,
              parentId: <Calendar['_id']>term.calendar,
              replacesUrl: true
            }))
          )
        }),
      ),
      ErrorCodes.ERR_TERM_EXISTS
    ).toPromise()
  }

  async removeTerm(term: Term) {
    if (!confirm('Are you sure you want to remove this term?')) return
    await this.popupService
      .runWithPopup(
        'Removing term',
        this.databaseService.database.remove.term(term._id).pipe(
          map(() => this.popViewEvent.emit())
        )
      )
      .toPromise()
  }

  goToCourse(course?: Course) {
    this.pushViewEvent.emit({
      name: ViewName.COURSE,
      docId: course?._id,
      parentId: <Term['_id']>course?.term,
      replacesUrl: true
    })
  }
}
