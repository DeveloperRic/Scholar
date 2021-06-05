import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { FormGroup, FormControl, Validators } from '@angular/forms'
import { ActivatedRoute } from '@angular/router'
import { ObjectId } from 'bson'
import { Observable, of, zip } from 'rxjs'
import { filter, map, switchMap, take } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Course as _Course } from 'src/app/model/course'
import { Test } from 'src/app/model/test'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName, TITLE_REGEX } from '../../manage.component'
import { UtilService } from 'src/app/services/util.service'
import { Term } from 'src/app/model/term'
import { Subject } from 'src/app/model/subject'

type Course = _Course & { subject: Subject }

@Component({
  selector: 'manage-test',
  templateUrl: './test.component.html',
  styleUrls: ['../../manage.component.css']
})
export class TestComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  @Output() popViewEvent = new EventEmitter<void>()
  testId$: Observable<Test['_id']>
  courseId$: Observable<Course['_id']>
  test$: Observable<Test>
  course$: Observable<Course>
  form: FormGroup
  startControlWasInvalid = false
  endControlWasInvalid = false

  constructor(
    private activatedRoute: ActivatedRoute,
    private databaseService: DatabaseService,
    private popupService: PopupService,
    private util: UtilService
  ) { }

  ngOnInit() {
    this.testId$ = this.activatedRoute.queryParamMap.pipe(
      map(queryParams => queryParams.get('docId'))
    )
    this.courseId$ = this.activatedRoute.queryParamMap.pipe(
      map(queryParams => queryParams.get('parentId')),
      filter(courseId => !!courseId)
      // TODO use shareReplay() / replay subject for things like this
    )
    this.test$ = this.testId$.pipe(
      switchMap(testId => {
        if (!testId) return of(null)
        return this.popupService.runWithPopup('Fetching test', this.databaseService.database.fetch.test(testId))
      }),
      switchMap(test => this.setForm(test))
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
  }

  setForm(test?: Test): Observable<Test> {
    return this.course$.pipe(
      switchMap(course => this.databaseService.database.fetch.term(<Term['_id']>course.term)),
      map(term => {
        const initialState = {
          title: test ? test.title : '',
          date: test ? this.util.toHTMLDatetime(test.date) : '',
          description: test ? test.description : '',
          scorePercent: test ? test.scorePercent : '',
          location: test ? JSON.stringify(test.location) : ''
        }
        this.form = new FormGroup({
          title: new FormControl(initialState.title, [Validators.required, Validators.pattern(TITLE_REGEX)]),
          date: new FormControl(initialState.date, [Validators.required, this.util.getDateValidator(new Date(term.start), new Date(term.end))]),
          description: new FormControl(initialState.description),
          scorePercent: new FormControl(initialState.scorePercent, [Validators.min(0), Validators.max(100)]),
          location: new FormControl(initialState.location, this.util.getJSONValidator<Location>('badLocation'))
        })
        return test
      })
    )
  }

  async submit(): Promise<void> {
    await this.popupService.runWithPopup(
      'Saving test',
      zip(this.testId$, this.courseId$).pipe(
        take(1),
        switchMap(([testId, courseId]) => {
          const test: Test = {
            _id: testId || new ObjectId().toHexString(),
            account: this.databaseService.accountId,
            course: courseId,
            title: this.form.get('title').value,
            date: new Date(this.form.get('date').value).getTime(),
            description: this.form.get('description').value
          }
          if (this.form.get('scorePercent').value) test.scorePercent = parseFloat(this.form.get('scorePercent').value)
          return this.databaseService.database.put.test(test).pipe(
            map(() => this.pushViewEvent.emit({
              name: ViewName.TEST,
              docId: test._id,
              parentId: <Course['_id']>test.course,
              replacesUrl: true
            }))
          )
        }),
      )
    ).toPromise()
  }

  async removeTest(test: Test) {
    if (!confirm('Are you sure you want to remove this test?')) return
    await this.popupService
      .runWithPopup(
        'Removing test',
        this.databaseService.database.remove.test(test._id).pipe(
          map(() => this.popViewEvent.emit())
        )
      )
      .toPromise()
  }
}
