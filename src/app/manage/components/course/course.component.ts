import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { FormGroup, FormControl, Validators } from '@angular/forms'
import { ActivatedRoute } from '@angular/router'
import { ObjectId } from 'bson'
import { Observable, of, zip } from 'rxjs'
import { filter, map, shareReplay, switchMap, take } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Term } from 'src/app/model/term'
import { Class } from 'src/app/model/class'
import { Course as _Course } from 'src/app/model/course'
import { ErrorCodes } from 'src/app/services/ErrorCodes'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'
import { Subject } from 'src/app/model/subject'
import { Test } from 'src/app/model/test'
import { Deliverable } from 'src/app/model/deliverable'
import { Teacher } from 'src/app/model/teacher'
import { Calendar } from 'src/app/model/calendar'
import { CODE_REGEX, TITLE_REGEX } from 'src/app/model/_model'

type Course = _Course & { subject: Subject }

@Component({
  selector: 'manage-course',
  templateUrl: './course.component.html',
  styleUrls: ['../../manage.component.css']
})
export class CourseComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  @Output() popViewEvent = new EventEmitter<void>()
  courseId$: Observable<Course['_id']>
  termId$: Observable<Term['_id']>
  course$: Observable<Course> //TODO do this typing for all areas where there is multi-fetching
  term$: Observable<Term>
  subjects$: Observable<Subject[]>
  teachers$: Observable<Teacher[]>
  classes$: Observable<Class[]>
  tests$: Observable<Test[]>
  deliverables$: Observable<Deliverable[]>
  hasClasses: boolean
  hasTests: boolean
  hasDeliverables: boolean
  form: FormGroup

  constructor(
    private activatedRoute: ActivatedRoute,
    private databaseService: DatabaseService,
    private popupService: PopupService
  ) { }

  ngOnInit() {
    this.courseId$ = this.activatedRoute.queryParamMap.pipe(
      map(queryParams => queryParams.get('docId'))
    )
    this.termId$ = this.activatedRoute.queryParamMap.pipe(
      map(queryParams => queryParams.get('parentId')),
      filter(termId => !!termId)
    )
    this.course$ = this.courseId$.pipe(
      switchMap(courseId => {
        if (!courseId) return of<Course>(null)
        //TODO make these popups cover the whole pipe (just use GraphQL, it will fix all these issues)
        return this.popupService.runWithPopup(
          'Fetching course',
          this.databaseService.database.fetch.course(courseId).pipe(
            switchMap(course => {
              // TODO notify user that doc has been deleted if they navigate via bookmark
              return this.databaseService.database.fetch.subject(<Subject['_id']>course.subject).pipe(
                map(subject => {
                  return <Course>{ ...course, subject }
                })
              )
            })
          )
        )
      }),
      map(course => this.setForm(course))
    )
    this.term$ = this.termId$.pipe(
      switchMap(termId => this.databaseService.database.fetch.term(termId)),
      shareReplay(1)
    )
    this.subjects$ = this.databaseService.database.all.subjects(this.databaseService.accountId)
    this.teachers$ = this.term$.pipe(
      switchMap(term => this.databaseService.database.all.teachers(<Calendar['_id']>term.calendar))
    )
    this.classes$ = this.courseId$.pipe(
      switchMap(courseId => {
        if (!courseId) return of([]) //TODO type these, it is causing static type checking holes (e.g. 'classes' below is of type any)
        return this.databaseService.database.all.classes(courseId)
      }),
      map(classes => {
        this.hasClasses = classes.length != 0
        return classes
      })
    )
    this.tests$ = this.courseId$.pipe(
      switchMap(courseId => {
        if (!courseId) return of([])
        return this.databaseService.database.all.tests(courseId)
      }),
      map(tests => {
        this.hasTests = tests.length != 0
        return tests
      })
    )
    this.deliverables$ = this.courseId$.pipe(
      switchMap(courseId => {
        if (!courseId) return of([])
        return this.databaseService.database.all.deliverables(courseId)
      }),
      map(deliverables => {
        this.hasDeliverables = deliverables.length != 0
        return deliverables
      })
    )
  }

  setForm(course?: Course): Course {
    const initialState = {
      code: course ? course.code : '',
      name: course ? course.name : '',
      subject: course ? course.subject._id : '',
      teacher: course ? course.teacher || '' : ''
    }
    this.form = new FormGroup({
      code: new FormControl(initialState.code, [Validators.required, Validators.pattern(CODE_REGEX)]),
      name: new FormControl(initialState.name, [Validators.required, Validators.pattern(TITLE_REGEX)]),
      subject: new FormControl(initialState.subject, [Validators.required, Validators.pattern(/^[a-f\d]{24}$/i)]),
      teacher: new FormControl(initialState.teacher, Validators.pattern(/^[a-f\d]{24}$/i))
    })
    return course
  }

  async submit() {
    await this.popupService.runWithPopup(
      'Saving course',
      zip(this.courseId$, this.termId$).pipe(
        take(1),
        switchMap(([courseId, termId]) => {
          const course: Course = {
            _id: courseId || new ObjectId().toHexString(),
            account: this.databaseService.accountId,
            term: termId,
            subject: this.form.get('subject').value,
            code: this.form.get('code').value,
            name: this.form.get('name').value
          }
          const teacher = this.form.get('teacher').value
          if (teacher) course.teacher = teacher
          return this.databaseService.database.put.course(course).pipe(
            map(() => this.pushViewEvent.emit({
              name: ViewName.COURSE,
              docId: course._id,
              parentId: <Term['_id']>course.term,
              replacesUrl: true
            }))
          )
        }),
      ),
      ErrorCodes.ERR_COURSE_EXISTS
    ).toPromise()
  }

  async removeCourse(course: Course) {
    if (!confirm('Are you sure you want to remove this course?')) return
    await this.popupService
      .runWithPopup(
        'Removing course',
        this.databaseService.database.remove.course(course._id).pipe(
          map(() => this.popViewEvent.emit())
        )
      )
      .toPromise()
  }

  async goToClass(klass?: Class) {
    await this.course$.pipe(
      take(1),
      map(course => {
        this.pushViewEvent.emit({
          name: ViewName.CLASS,
          docId: klass?._id,
          parentId: <Course['_id']>klass?.course || course._id
        })
      })
    ).toPromise()
  }

  async goToTest(test?: Test) {
    await this.course$.pipe(
      take(1),
      map(course => {
        this.pushViewEvent.emit({
          name: ViewName.TEST,
          docId: test?._id,
          parentId: <Course['_id']>test?.course || course._id
        })
      })
    ).toPromise()
  }

  async goToDeliverable(deliverable?: Deliverable) {
    await this.course$.pipe(
      take(1),
      map(course => {
        this.pushViewEvent.emit({
          name: ViewName.DELIVERABLE,
          docId: deliverable?._id,
          parentId: <Course['_id']>deliverable?.course || course._id
        })
      })
    ).toPromise()
  }
}
