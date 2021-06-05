import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { Observable, of, ReplaySubject } from 'rxjs'
import { concatMap, map, switchMap, take, toArray } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Course as _Course } from 'src/app/model/course'
import { Test as _Test } from 'src/app/model/test'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'
import { FilterField } from '../filter-menu/filter-menu.component'
import { Subject } from 'src/app/model/subject'

type Course = _Course & { subject: Subject }
type Test = _Test & { course: Course }

@Component({
  selector: 'manage-tests',
  templateUrl: './tests.component.html',
  styleUrls: ['../../manage.component.css']
})
export class TestsComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  tests$: Observable<Test[]>
  course$: Observable<Course>
  hasTests: boolean
  filterMenuVisible: boolean
  enabledFilters = [FilterField.COURSE]
  selectedCourseId$ = new ReplaySubject<Course['_id']>(1)

  constructor(private databaseService: DatabaseService, private popupService: PopupService) { }

  ngOnInit() {
    this.setTests$()
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

  private setTests$() {
    if (this.tests$) return
    this.tests$ = this.selectedCourseId$.pipe(
      switchMap(selectedCourseId => {
        return this.popupService.runWithPopup(
          'Fetching tests',
          of(undefined).pipe(
            switchMap(() => {
              if (selectedCourseId) return this.getTestsFromSelectedCourse()
              return this.getTestsFromAllCourses()
            }),
            map(tests => {
              this.hasTests = tests.length != 0
              return tests
            })
          )
        )
      })
    )
  }

  private getTestsFromSelectedCourse(): Observable<Test[]> {
    return this.course$.pipe(
      switchMap(course => {
        return this.databaseService.database.all.tests(course._id).pipe(
          switchMap(tests => of(...tests)),
          map(test => {
            return <Test>{ ...test, course }
          }),
          toArray()
        )
      })
    )
  }

  private getTestsFromAllCourses(): Observable<Test[]> {
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
          return this.databaseService.database.all.tests(course._id).pipe(
            concatMap(tests => of(...tests)), // create a strem of tests (gathered from each course)
            map(test => {
              return <Test>{ ...test, course }
            })
          )
        }),
        toArray()
      ))
    )
  }

  async goToTest(test?: Test) {
    await this.selectedCourseId$.pipe(
      take(1),
      map(selectedCourseId => this.pushViewEvent.emit({
        name: ViewName.TEST,
        docId: test?._id,
        parentId: (<Course>test?.course)?._id || selectedCourseId
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
