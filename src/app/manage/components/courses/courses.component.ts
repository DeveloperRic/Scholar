import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { Observable, of, ReplaySubject } from 'rxjs'
import { concatMap, map, switchMap, take, toArray } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Term } from 'src/app/model/term'
import { Course } from 'src/app/model/course'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'
import { FilterField } from '../filter-menu/filter-menu.component'

@Component({
  selector: 'manage-courses',
  templateUrl: './courses.component.html',
  styleUrls: ['../../manage.component.css']
})
export class CoursesComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  courses$: Observable<Course[]>
  term$: Observable<Term>
  hasCourses: boolean
  filterMenuVisible: boolean
  enabledFilters = [FilterField.TERM]
  selectedTermId$ = new ReplaySubject<Term['_id']>(1)

  constructor(private databaseService: DatabaseService, private popupService: PopupService) { }

  ngOnInit() {
    this.setCourses$()
    this.selectedTermId$.next(undefined) // initial value
    this.term$ = this.selectedTermId$.pipe(
      switchMap(selectedTermId => this.databaseService.database.fetch.term(selectedTermId))
    )
  }

  private setCourses$() {
    if (this.courses$) return
    this.courses$ = this.selectedTermId$.pipe(
      switchMap(selectedTermId => {
        return this.popupService.runWithPopup(
          'Fetching courses',
          of(undefined).pipe(
            switchMap(() => {
              if (selectedTermId) return this.databaseService.database.all.courses(selectedTermId)
              return this.getCoursesFromAllTerms()
            }),
            map(courses => {
              this.hasCourses = courses.length != 0
              return courses
            })
          )
        )
      })
    )
  }

  private getCoursesFromAllTerms(): Observable<Course[]> {
    return this.databaseService.database.all.calendars(this.databaseService.accountId).pipe(
      concatMap(calendars => of(...calendars).pipe(
        concatMap(calendar => this.databaseService.database.all.terms(calendar._id).pipe(
          concatMap(terms => of(...terms).pipe(
            concatMap(term => this.databaseService.database.all.courses(term._id)),
            concatMap(courses => of(...courses)) // create a strem of courses (gathered from each term from each calendar)
          ))
        )),
        toArray()
      ))
    )
  }

  async goToCourse(course?: Course) {
    await this.selectedTermId$.pipe(
      take(1),
      map(selectedTermId => this.pushViewEvent.emit({
        name: ViewName.COURSE,
        docId: course?._id,
        parentId: <Term['_id']>course?.term || selectedTermId
      }))
    ).toPromise()
  }

  toggleFilterMenu() {
    this.filterMenuVisible = !this.filterMenuVisible
  }

  onFilterChange(formData: { term: Term['_id'] }) {
    this.selectedTermId$.next(formData.term || undefined) // prevent ObjectId cast errors when '' is returned
  }
}
