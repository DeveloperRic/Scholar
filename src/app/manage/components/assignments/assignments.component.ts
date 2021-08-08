import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { Observable, of, ReplaySubject } from 'rxjs'
import { concatMap, map, switchMap, take, toArray } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Course as _Course } from 'src/app/model/course'
import { Deliverable as _Assignment } from 'src/app/model/deliverable'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'
import { FilterField } from '../filter-menu/filter-menu.component'
import { Subject } from 'src/app/model/subject'

type Course = _Course & { subject: Subject }
type Assignment = _Assignment & { course: Course }

@Component({
  selector: 'manage-assignments',
  templateUrl: './assignments.component.html',
  styleUrls: ['../../manage.component.css']
})
export class AssignmentsComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  assignments$: Observable<Assignment[]>
  course$: Observable<Course>
  hasAssignments: boolean
  filterMenuVisible: boolean
  enabledFilters = [FilterField.COURSE]
  selectedCourseId$ = new ReplaySubject<Course['_id']>(1)

  constructor(private databaseService: DatabaseService, private popupService: PopupService) { }

  ngOnInit() {
    this.setAssignments$()
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

  private setAssignments$() {
    if (this.assignments$) return
    this.assignments$ = this.selectedCourseId$.pipe(
      switchMap(selectedCourseId => {
        return this.popupService.runWithPopup(
          'Fetching assignments',
          of(undefined).pipe(
            switchMap(() => {
              if (selectedCourseId) return this.getAssignmentsFromSelectedCourse()
              return this.getAssignmentsFromAllCourses()
            }),
            map(assignments => {
              this.hasAssignments = assignments.length != 0
              return assignments
            })
          )
        )
      })
    )
  }

  private getAssignmentsFromSelectedCourse(): Observable<Assignment[]> {
    return this.course$.pipe(
      switchMap(course => {
        return this.databaseService.database.all.deliverables(course._id).pipe(
          switchMap(assignments => of(...assignments)),
          map(assignment => {
            return <Assignment>{ ...assignment, course }
          }),
          toArray()
        )
      })
    )
  }

  private getAssignmentsFromAllCourses(): Observable<Assignment[]> {
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
            concatMap(assignments => of(...assignments)), // create a strem of assignments (gathered from each course)
            map(assignment => {
              return <Assignment>{ ...assignment, course }
            })
          )
        }),
        toArray()
      ))
    )
  }

  async goToAssignment(assignment?: Assignment) {
    await this.selectedCourseId$.pipe(
      take(1),
      map(selectedCourseId => this.pushViewEvent.emit({
        name: ViewName.ASSIGNMENT,
        docId: assignment?._id,
        parentId: (<Course>assignment?.course)?._id || selectedCourseId
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
