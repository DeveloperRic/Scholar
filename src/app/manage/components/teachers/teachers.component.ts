import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { Observable, of, ReplaySubject } from 'rxjs'
import { concatMap, map, switchMap, take, toArray } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Calendar } from 'src/app/model/calendar'
import { Teacher } from 'src/app/model/teacher'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'
import { FilterField } from '../filter-menu/filter-menu.component'

@Component({
  selector: 'manage-teachers',
  templateUrl: './teachers.component.html',
  styleUrls: ['../../manage.component.css']
})
export class TeachersComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  teachers$: Observable<Teacher[]>
  calendar$: Observable<Calendar>
  hasTeachers: boolean
  filterMenuVisible: boolean
  enabledFilters = [FilterField.CALENDAR]
  selectedCalendarId$ = new ReplaySubject<Calendar['_id']>(1)

  constructor(private databaseService: DatabaseService, private popupService: PopupService) { }

  ngOnInit() {
    this.setTeachers$()
    this.selectedCalendarId$.next(undefined) // initial value
    this.calendar$ = this.selectedCalendarId$.pipe(
      switchMap(selectedCalendarId => this.databaseService.database.fetch.calendar(selectedCalendarId))
    )
  }

  private setTeachers$() {
    if (this.teachers$) return
    this.teachers$ = this.selectedCalendarId$.pipe(
      switchMap(selectedCalendarId => {
        return this.popupService.runWithPopup(
          'Fetching teachers',
          of(undefined).pipe(
            switchMap(() => {
              if (selectedCalendarId) return this.databaseService.database.all.teachers(selectedCalendarId)
              return this.getTeachersFromAllCalendars()
            }),
            map(teachers => {
              this.hasTeachers = teachers.length != 0
              return teachers
            })
          )
        )
      })
    )
  }

  private getTeachersFromAllCalendars(): Observable<Teacher[]> {
    return this.databaseService.database.all.calendars(this.databaseService.accountId).pipe(
      switchMap(calendars => of(...calendars).pipe(
        concatMap(calendar => {
          return this.databaseService.database.all.teachers(calendar._id).pipe(
            map(teachers => teachers.map(teacher => ({ ...teacher, calendar: calendar })))
          )
        }),
        concatMap(teachers => of(...teachers)), // create a strem of teachers (gathered from each calendar)
        toArray()
      ))
    )
  }

  async goToTeacher(teacher?: Teacher) {
    await this.selectedCalendarId$.pipe(
      take(1),
      map(selectedCalendarId => this.pushViewEvent.emit({
        name: ViewName.TEACHER,
        docId: teacher?._id,
        parentId: (<Calendar>teacher?.calendar)?._id || selectedCalendarId
      }))
    ).toPromise()
  }

  toggleFilterMenu() {
    this.filterMenuVisible = !this.filterMenuVisible
  }

  onFilterChange(formData: { calendar: Calendar['_id'] }) {
    this.selectedCalendarId$.next(formData.calendar || undefined) // prevent ObjectId cast errors when '' is returned
  }
}
