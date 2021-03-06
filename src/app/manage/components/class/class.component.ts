import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { FormGroup, FormControl, Validators, ValidatorFn } from '@angular/forms'
import { ActivatedRoute } from '@angular/router'
import { ObjectId } from 'bson'
import { Observable, of, zip } from 'rxjs'
import { filter, map, switchMap, take } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Course as _Course } from 'src/app/model/course'
import { Class } from 'src/app/model/class'
import { ErrorCodes } from 'src/app/services/ErrorCodes'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'
import { RelativeWeeklyScheduleRepeat, ScheduleRepeatBasis, ScheduleRepeatDay } from 'src/app/model/schedule'
import { UtilService } from 'src/app/services/util.service'
import { Teacher } from 'src/app/model/teacher'
import { Term } from 'src/app/model/term'
import { Calendar } from 'src/app/model/calendar'
import { Subject } from 'src/app/model/subject'
import { StartEndDateValidator } from 'src/app/services/start-end-date.validator'
import { CODE_REGEX } from 'src/app/model/_model'

type Course = _Course & { subject: Subject }

@Component({
  selector: 'manage-class',
  templateUrl: './class.component.html',
  styleUrls: ['../../manage.component.css']
})
export class ClassComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  @Output() popViewEvent = new EventEmitter<void>()
  classId$: Observable<Class['_id']>
  courseId$: Observable<Course['_id']>
  class$: Observable<Class>
  course$: Observable<Course>
  teachers$: Observable<Teacher[]>
  form: FormGroup

  constructor(
    private activatedRoute: ActivatedRoute,
    private databaseService: DatabaseService,
    private popupService: PopupService,
    private util: UtilService
  ) { }

  ngOnInit() {
    this.classId$ = this.activatedRoute.queryParamMap.pipe(
      map(queryParams => queryParams.get('docId'))
    )
    this.courseId$ = this.activatedRoute.queryParamMap.pipe(
      map(queryParams => queryParams.get('parentId')),
      filter(courseId => !!courseId)
      // TODO use shareReplay() / replay subject for things like this
    )
    this.class$ = this.classId$.pipe(
      switchMap(classId => {
        if (!classId) return of(null)
        return this.popupService.runWithPopup('Fetching class', this.databaseService.database.fetch.class(classId))
      }),
      switchMap(klass => this.setForm(klass))
    )
    this.course$ = this.courseId$.pipe(
      switchMap(courseId => this.databaseService.database.fetch.course(courseId)),
      switchMap(course => {
        if (!course) return of(null)
        return this.databaseService.database.fetch.subject(<Subject['_id']>course.subject).pipe(
          map(subject => {
            return <Course>{ ...course, subject }
          })
        )
      })
    )
    this.teachers$ = this.course$.pipe(
      switchMap(course => this.databaseService.database.fetch.term(<Term['_id']>course.term)),
      switchMap(term => this.databaseService.database.fetch.calendar(<Calendar['_id']>term.calendar)),
      switchMap(calendar => this.databaseService.database.all.teachers(calendar._id))
    )
  }

  setForm(klass?: Class): Observable<Class> {
    return this.course$.pipe(
      map(course => {
        const initialState = {
          code: klass ? klass.code : '',
          start: klass ? klass.start : '08:00',
          end: klass ? klass.end : '09:30',
          repeat: Object.keys(ScheduleRepeatDay).reduce((obj, key) => {
            obj[key] = klass ? klass.repeat.days.includes(ScheduleRepeatDay[key]) : false
            return obj
          }, {}),
          teacher: klass ? klass.teacher || '' : course.teacher,
          location: klass ? JSON.stringify(klass.location) : ''
        }
        const startEndValidators = new StartEndDateValidator('start', 'end', this.util.convertInputTimeStringToDate)
        this.form = new FormGroup({
          code: new FormControl(initialState.code, [Validators.required, Validators.pattern(CODE_REGEX)]),
          start: new FormControl(initialState.start, [Validators.required, this.util.getDateValidator()]),
          end: new FormControl(initialState.end, [Validators.required, this.util.getDateValidator(), startEndValidators.endValidator]),
          // schedule: new FormControl(initialState.schedule, [Validators.required, this.getJSONValidator<Schedule>('badSchedule')]),
          repeat: new FormGroup(
            Object.keys(initialState.repeat).reduce((obj, key) => {
              obj[key] = new FormControl(initialState.repeat[key])
              return obj
            }, {}),
            this.getRepeatValidator()
          ),
          teacher: new FormControl(initialState.teacher, Validators.pattern(/^[a-f\d]{24}$/i)),
          location: new FormControl(initialState.location, this.util.getJSONValidator<Location>('badLocation'))
        })
        startEndValidators.setForm(this.form)
        return klass
      })
    )
  }

  private getRepeatValidator(): ValidatorFn {
    return (group: FormGroup) => {
      const controls = Object.keys(ScheduleRepeatDay).map(dayName => group.controls[dayName])
      for (const control of controls) {
        if (control.value === true) return {}
      }
      const errorObject = Object.keys(ScheduleRepeatDay).map(dayName => {
        const control = group.controls[dayName]
        return { dayName: control.value }
      })
      return { noDaySelected: errorObject }
    }
  }

  async submit(): Promise<void> {
    await this.popupService.runWithPopup(
      'Saving class',
      zip(this.classId$, this.courseId$).pipe(
        take(1),
        switchMap(([classId, courseId]) => {
          const klass: Class = {
            _id: classId || new ObjectId().toHexString(),
            account: this.databaseService.accountId,
            course: courseId,
            code: this.form.get('code').value,
            start: this.form.get('start').value,
            end: this.form.get('end').value,
            repeat: <RelativeWeeklyScheduleRepeat>{
              basis: ScheduleRepeatBasis.WEEK,
              days: Object.keys(ScheduleRepeatDay)
                .filter(key => (<FormGroup>this.form.get('repeat')).get(key).value)
                .map(key => ScheduleRepeatDay[key])
            }
          }
          const teacher = this.form.get('teacher').value
          if (teacher) klass.teacher = teacher
          return this.databaseService.database.put.class(klass).pipe(
            map(() => this.pushViewEvent.emit({
              name: ViewName.CLASS,
              docId: klass._id,
              parentId: <Course['_id']>klass.course,
              replacesUrl: true
            }))
          )
        }),
      ),
      ErrorCodes.ERR_CLASS_EXISTS
    ).toPromise()
  }

  async removeClass(klass: Class) {
    if (!confirm('Are you sure you want to remove this class?')) return
    await this.popupService
      .runWithPopup(
        'Removing class',
        this.databaseService.database.remove.class(klass._id).pipe(
          map(() => this.popViewEvent.emit())
        )
      )
      .toPromise()
  }
}
