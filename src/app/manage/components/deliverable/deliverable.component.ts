import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { FormGroup, FormControl, Validators } from '@angular/forms'
import { ActivatedRoute } from '@angular/router'
import { ObjectId } from 'bson'
import { Observable, of, zip } from 'rxjs'
import { filter, map, switchMap, take } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Course as _Course } from 'src/app/model/course'
import { Deliverable } from 'src/app/model/deliverable'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'
import { UtilService } from 'src/app/services/util.service'
import { Term } from 'src/app/model/term'
import { Subject } from 'src/app/model/subject'
import { TITLE_REGEX } from 'src/app/model/_model'

type Course = _Course & { subject: Subject }

@Component({
  selector: 'manage-deliverable',
  templateUrl: './deliverable.component.html',
  styleUrls: ['../../manage.component.css']
})
export class DeliverableComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  @Output() popViewEvent = new EventEmitter<void>()
  deliverableId$: Observable<Deliverable['_id']>
  courseId$: Observable<Course['_id']>
  deliverable$: Observable<Deliverable>
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
    this.deliverableId$ = this.activatedRoute.queryParamMap.pipe(
      map(queryParams => queryParams.get('docId'))
    )
    this.courseId$ = this.activatedRoute.queryParamMap.pipe(
      map(queryParams => queryParams.get('parentId')),
      filter(courseId => !!courseId)
      // TODO use shareReplay() / replay subject for things like this
    )
    this.deliverable$ = this.deliverableId$.pipe(
      switchMap(deliverableId => {
        if (!deliverableId) return of(null)
        return this.popupService.runWithPopup('Fetching deliverable', this.databaseService.database.fetch.deliverable(deliverableId))
      }),
      switchMap(deliverable => this.setForm(deliverable))
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

  setForm(deliverable?: Deliverable): Observable<Deliverable> {
    return this.course$.pipe(
      switchMap(course => this.databaseService.database.fetch.term(<Term['_id']>course.term)),
      map(term => {
        const initialState = {
          title: deliverable ? deliverable.title : '',
          deadline: deliverable ? this.util.toHTMLDatetime(deliverable.deadline) : '',
          description: deliverable ? deliverable.description : '',
          percentComplete: deliverable ? deliverable.percentComplete : 0
        }
        this.form = new FormGroup({
          title: new FormControl(initialState.title, [Validators.required, Validators.pattern(TITLE_REGEX)]),
          deadline: new FormControl(initialState.deadline, [Validators.required, this.util.getDateValidator(new Date(term.start), new Date(term.end))]),
          description: new FormControl(initialState.description),
          percentComplete: new FormControl(initialState.percentComplete, [Validators.required, Validators.min(0), Validators.max(100)])
        })
        return deliverable
      })
    )
  }

  async submit(): Promise<void> {
    await this.popupService.runWithPopup(
      'Saving deliverable',
      zip(this.deliverableId$, this.courseId$).pipe(
        take(1),
        switchMap(([deliverableId, courseId]) => {
          const deliverable: Deliverable = {
            _id: deliverableId || new ObjectId().toHexString(),
            account: this.databaseService.accountId,
            course: courseId,
            title: this.form.get('title').value,
            description: this.form.get('description').value,
            deadline: new Date(this.form.get('deadline').value).getTime(),
            percentComplete: parseFloat(this.form.get('percentComplete').value)
          }
          return this.databaseService.database.put.deliverable(deliverable).pipe(
            map(() => this.pushViewEvent.emit({
              name: ViewName.DELIVERABLE,
              docId: deliverable._id,
              parentId: <Course['_id']>deliverable.course,
              replacesUrl: true
            }))
          )
        }),
      )
    ).toPromise()
  }

  async removeDeliverable(deliverable: Deliverable) {
    if (!confirm('Are you sure you want to remove this deliverable?')) return
    await this.popupService
      .runWithPopup(
        'Removing deliverable',
        this.databaseService.database.remove.deliverable(deliverable._id).pipe(
          map(() => this.popViewEvent.emit())
        )
      )
      .toPromise()
  }
}
