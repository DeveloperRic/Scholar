import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { Observable, of, ReplaySubject } from 'rxjs'
import { concatMap, map, switchMap, take, toArray } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Course as _Course } from 'src/app/model/course'
import { Deliverable as _Deliverable } from 'src/app/model/deliverable'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'
import { FilterField } from '../filter-menu/filter-menu.component'
import { Subject } from 'src/app/model/subject'

type Course = _Course & { subject: Subject }
type Deliverable = _Deliverable & { course: Course }

@Component({
  selector: 'manage-deliverables',
  templateUrl: './deliverables.component.html',
  styleUrls: ['../../manage.component.css']
})
export class DeliverablesComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  deliverables$: Observable<Deliverable[]>
  course$: Observable<Course>
  hasDeliverables: boolean
  filterMenuVisible: boolean
  enabledFilters = [FilterField.COURSE]
  selectedCourseId$ = new ReplaySubject<Course['_id']>(1)

  constructor(private databaseService: DatabaseService, private popupService: PopupService) { }

  ngOnInit() {
    this.setDeliverables$()
    this.selectedCourseId$.next(undefined) // initial value
    this.course$ = this.selectedCourseId$.pipe(
      switchMap(selectedCourseId => this.databaseService.database.fetch.course(selectedCourseId)),
      switchMap(course => {
        if (!course) return of(undefined)
        return this.databaseService.database.fetch.subject(<Subject['_id']>course.subject).pipe(
          map(subject => {
            return <Course>{ ...course, subject }
          })
        )
      })
    )
  }

  private setDeliverables$() {
    if (this.deliverables$) return
    this.deliverables$ = this.selectedCourseId$.pipe(
      switchMap(selectedCourseId => {
        return this.popupService.runWithPopup(
          'Fetching deliverables',
          of(undefined).pipe(
            switchMap(() => {
              if (selectedCourseId) return this.getDeliverablesFromSelectedCourse()
              return this.getDeliverablesFromAllCourses()
            }),
            map(deliverables => {
              this.hasDeliverables = deliverables.length != 0
              return deliverables
            })
          )
        )
      })
    )
  }

  private getDeliverablesFromSelectedCourse(): Observable<Deliverable[]> {
    return this.course$.pipe(
      switchMap(course => {
        return this.databaseService.database.all.deliverables(course._id).pipe(
          switchMap(deliverables => of(...deliverables)),
          map(deliverable => {
            return <Deliverable>{ ...deliverable, course }
          }),
          toArray()
        )
      })
    )
  }

  private getDeliverablesFromAllCourses(): Observable<Deliverable[]> {
    return this.databaseService.database.all.calendars(this.databaseService.accountId).pipe(
      switchMap(calendars => of(...calendars).pipe(
        concatMap(calendar => this.databaseService.database.all.terms(calendar._id)),
        concatMap(terms => of(...terms)),
        concatMap(term => this.databaseService.database.all.courses(term._id)),
        concatMap(courses => of(...courses)),
        concatMap(course => {
          return this.databaseService.database.fetch.subject(<Subject['_id']>course.subject).pipe(
            map(subject => {
              return <Course>{ ...course, subject }
            })
          )
        }),
        concatMap(course => {
          return this.databaseService.database.all.deliverables(course._id).pipe(
            concatMap(deliverables => of(...deliverables)), // create a strem of deliverables (gathered from each course)
            map(deliverable => {
              return <Deliverable>{ ...deliverable, course }
            })
          )
        }),
        toArray()
      ))
    )
  }

  async goToDeliverable(deliverable?: Deliverable) {
    await this.selectedCourseId$.pipe(
      take(1),
      map(selectedCourseId => this.pushViewEvent.emit({
        name: ViewName.DELIVERABLE,
        docId: deliverable?._id,
        parentId: (<Course>deliverable?.course)?._id || selectedCourseId
      }))
    ).toPromise()
  }

  toggleFilterMenu() {
    this.filterMenuVisible = !this.filterMenuVisible
  }

  onFilterChange(formData: { course: Course['_id'] }) {
    this.selectedCourseId$.next(formData.course || undefined) // prevent ObjectId cast errors when '' is returned
  }
}
