import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { Observable, of, ReplaySubject } from 'rxjs'
import { concatMap, map, switchMap, take, toArray } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Course as _Course } from 'src/app/model/course'
import { Class as _Class } from 'src/app/model/class'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'
import { FilterField } from '../filter-menu/filter-menu.component'
import { Subject } from 'src/app/model/subject'

type Course = _Course & { subject: Subject }
type Class = _Class & { course: Course }

@Component({
  selector: 'manage-classes',
  templateUrl: './classes.component.html',
  styleUrls: ['../../manage.component.css']
})
export class ClassesComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  classes$: Observable<Class[]>
  course$: Observable<Course>
  hasClasses: boolean
  filterMenuVisible: boolean
  enabledFilters = [FilterField.COURSE]
  selectedCourseId$ = new ReplaySubject<Course['_id']>(1)

  constructor(private databaseService: DatabaseService, private popupService: PopupService) { }

  ngOnInit() {
    this.setClasses$()
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

  private setClasses$() {
    if (this.classes$) return
    this.classes$ = this.selectedCourseId$.pipe(
      switchMap(selectedCourseId => {
        return this.popupService.runWithPopup(
          'Fetching classes',
          of(undefined).pipe(
            switchMap(() => {
              if (selectedCourseId) return this.getClassesFromSelectedCourse()
              return this.getClassesFromAllCourses()
            }),
            map(classes => {
              this.hasClasses = classes.length != 0
              return classes
            })
          )
        )
      })
    )
  }

  private getClassesFromSelectedCourse(): Observable<Class[]> {
    return this.course$.pipe(
      switchMap(course => {
        return this.databaseService.database.all.classes(course._id).pipe(
          switchMap(classes => of(...classes)),
          map(klass => {
            return <Class>{ ...klass, course }
          }),
          toArray()
        )
      })
    )
  }

  private getClassesFromAllCourses(): Observable<Class[]> {
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
          return this.databaseService.database.all.classes(course._id).pipe(
            concatMap(classes => of(...classes)), // create a strem of classes (gathered from each course)
            map(klass => {
              return <Class>{ ...klass, course }
            })
          )
        }),
        toArray()
      ))
    )
  }

  async goToClass(klass?: Class) {
    await this.selectedCourseId$.pipe(
      take(1),
      map(selectedCourseId => this.pushViewEvent.emit({
        name: ViewName.CLASS,
        docId: klass?._id,
        parentId: (<Course>klass?.course)?._id || selectedCourseId
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
