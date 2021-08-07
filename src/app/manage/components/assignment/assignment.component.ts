import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { FormGroup, FormControl, Validators } from '@angular/forms'
import { ActivatedRoute } from '@angular/router'
import { ObjectId } from 'bson'
import { Observable, of, zip } from 'rxjs'
import { filter, map, switchMap, take } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Course as _Course } from 'src/app/model/course'
import { Deliverable as Assignment } from 'src/app/model/deliverable'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'
import { UtilService } from 'src/app/services/util.service'
import { Term } from 'src/app/model/term'
import { Subject } from 'src/app/model/subject'
import { TITLE_REGEX } from 'src/app/model/_model'

type Course = _Course & { subject: Subject }

@Component({
  selector: 'manage-assignment',
  templateUrl: './assignment.component.html',
  styleUrls: ['../../manage.component.css']
})
export class AssignmentComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  @Output() popViewEvent = new EventEmitter<void>()
  assignmentId$: Observable<Assignment['_id']>
  courseId$: Observable<Course['_id']>
  assignment$: Observable<Assignment>
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
    this.assignmentId$ = this.activatedRoute.queryParamMap.pipe(
      map(queryParams => queryParams.get('docId'))
    )
    this.courseId$ = this.activatedRoute.queryParamMap.pipe(
      map(queryParams => queryParams.get('parentId')),
      filter(courseId => !!courseId)
      // TODO use shareReplay() / replay subject for things like this
    )
    this.assignment$ = this.assignmentId$.pipe(
      switchMap(assignmentId => {
        if (!assignmentId) return of(null)
        return this.popupService.runWithPopup('Fetching assignment', this.databaseService.database.fetch.deliverable(assignmentId))
      }),
      switchMap(assignment => this.setForm(assignment))
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

  setForm(assignment?: Assignment): Observable<Assignment> {
    return this.course$.pipe(
      switchMap(course => this.databaseService.database.fetch.term(<Term['_id']>course.term)),
      map(term => {
        const initialState = {
          title: assignment ? assignment.title : '',
          deadline: assignment ? this.util.toHTMLDatetime(assignment.deadline) : '',
          description: assignment ? assignment.description : '',
          percentComplete: assignment ? assignment.percentComplete : 0
        }
        this.form = new FormGroup({
          title: new FormControl(initialState.title, [Validators.required, Validators.pattern(TITLE_REGEX)]),
          deadline: new FormControl(initialState.deadline, [Validators.required, this.util.getDateValidator(new Date(term.start), new Date(term.end))]),
          description: new FormControl(initialState.description),
          percentComplete: new FormControl(initialState.percentComplete, [Validators.required, Validators.min(0), Validators.max(100)])
        })
        return assignment
      })
    )
  }

  async submit(): Promise<void> {
    await this.popupService.runWithPopup(
      'Saving assignment',
      zip(this.assignmentId$, this.courseId$).pipe(
        take(1),
        switchMap(([assignmentId, courseId]) => {
          const assignment: Assignment = {
            _id: assignmentId || new ObjectId().toHexString(),
            account: this.databaseService.accountId,
            course: courseId,
            title: this.form.get('title').value,
            description: this.form.get('description').value,
            deadline: new Date(this.form.get('deadline').value).getTime(),
            percentComplete: parseFloat(this.form.get('percentComplete').value)
          }
          return this.databaseService.database.put.deliverable(assignment).pipe(
            map(() => this.pushViewEvent.emit({
              name: ViewName.ASSIGNMENT,
              docId: assignment._id,
              parentId: <Course['_id']>assignment.course,
              replacesUrl: true
            }))
          )
        }),
      )
    ).toPromise()
  }

  async removeAssignment(assignment: Assignment) {
    if (!confirm('Are you sure you want to remove this assignment?')) return
    await this.popupService
      .runWithPopup(
        'Removing assignment',
        this.databaseService.database.remove.deliverable(assignment._id).pipe(
          map(() => this.popViewEvent.emit())
        )
      )
      .toPromise()
  }
}
