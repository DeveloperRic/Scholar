import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { FormGroup, FormControl, Validators } from '@angular/forms'
import { ActivatedRoute } from '@angular/router'
import { ObjectId } from 'bson'
import { Observable, of, zip } from 'rxjs'
import { filter, map, switchMap, take } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Calendar } from 'src/app/model/calendar'
import { Course } from 'src/app/model/course'
import { Teacher } from 'src/app/model/teacher'
import { ErrorCodes } from 'src/app/services/ErrorCodes'
import { PopupService } from 'src/app/services/popup.service'
import { UtilService } from 'src/app/services/util.service'
import { ViewInfo, TITLE_REGEX, ViewName, NAME_REGEX } from '../../manage.component'

@Component({
  selector: 'manage-teacher',
  templateUrl: './teacher.component.html'
})
export class TeacherComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  @Output() popViewEvent = new EventEmitter<void>()
  teacherId$: Observable<Teacher['_id']>
  calendarId$: Observable<Calendar['_id']>
  teacher$: Observable<Teacher>
  calendar$: Observable<Calendar>
  form: FormGroup

  constructor(
    private activatedRoute: ActivatedRoute,
    private databaseService: DatabaseService,
    private popupService: PopupService,
    private util: UtilService
  ) { }

  ngOnInit() {
    this.teacherId$ = this.activatedRoute.queryParamMap.pipe(map(queryParams => queryParams.get('docId')))
    this.calendarId$ = this.activatedRoute.queryParamMap.pipe(
      map(queryParams => queryParams.get('parentId')),
      filter(calendarId => !!calendarId)
      // TODO use share() / replay subject for things like this
    )
    this.teacher$ = this.teacherId$.pipe(
      switchMap(teacherId => {
        if (!teacherId) return of(null)
        return this.popupService.runWithPopup('Fetching teacher', this.databaseService.database.fetch.teacher(teacherId))
      }),
      map(teacher => this.setForm(teacher))
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
      ))
    )
  }

  setForm(teacher?: Teacher): Teacher {
    const initialState = {
      firstName: teacher ? teacher.firstName : '',
      lastName: teacher ? teacher.lastName : '',
      email: teacher ? teacher.email : ''
    }
    this.form = new FormGroup({
      firstName: new FormControl(initialState.firstName, [Validators.required, Validators.pattern(NAME_REGEX)]),
      lastName: new FormControl(initialState.lastName, [Validators.required, Validators.pattern(NAME_REGEX)]),
      email: new FormControl(initialState.email, Validators.email)
    })
    return teacher
  }

  async submit(): Promise<void> {
    await this.popupService.runWithPopup(
      'Saving teacher',
      zip(this.teacherId$, this.calendarId$).pipe(
        take(1),
        switchMap(([teacherId, calendarId]) => {
          const teacher: Teacher = {
            _id: teacherId || new ObjectId().toHexString(),
            account: this.databaseService.accountId,
            calendar: calendarId,
            firstName: this.form.get('firstName').value,
            lastName: this.form.get('lastName').value,
            email: this.form.get('email').value
          }
          return this.databaseService.database.put.teacher(teacher).pipe(
            map(() => this.pushViewEvent.emit({
              name: ViewName.TEACHER,
              docId: teacher._id,
              parentId: <Calendar['_id']>teacher.calendar,
              replacesUrl: true
            }))
          )
        }),
      ),
      ErrorCodes.ERR_TEACHER_EXISTS
    ).toPromise()
  }

  async removeTeacher(teacher: Teacher) {
    if (!confirm('Are you sure you want to remove this teacher?')) return
    await this.popupService
      .runWithPopup(
        'Removing teacher',
        this.databaseService.database.remove.teacher(teacher._id).pipe(
          map(() => this.popViewEvent.emit())
        )
      )
      .toPromise()
  }

  goToCourse(course?: Course) {
    this.pushViewEvent.emit({
      name: ViewName.COURSE,
      docId: course?._id,
      parentId: <Teacher['_id']>course?.teacher,
      replacesUrl: true
    })
  }
}
