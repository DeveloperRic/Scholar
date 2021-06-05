import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { Observable, of, ReplaySubject } from 'rxjs'
import { concatMap, map, switchMap, take, toArray } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Calendar } from 'src/app/model/calendar'
import { Term } from 'src/app/model/term'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'
import { FilterField } from '../filter-menu/filter-menu.component'

@Component({
  selector: 'manage-terms',
  templateUrl: './terms.component.html',
  styleUrls: ['../../manage.component.css']
})
export class TermsComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  terms$: Observable<Term[]>
  calendar$: Observable<Calendar>
  hasTerms: boolean
  filterMenuVisible: boolean
  enabledFilters = [FilterField.CALENDAR]
  selectedCalendarId$ = new ReplaySubject<Calendar['_id']>(1)

  constructor(private databaseService: DatabaseService, private popupService: PopupService) { }

  ngOnInit() {
    this.setTerms$()
    this.selectedCalendarId$.next(undefined) // initial value
    this.calendar$ = this.selectedCalendarId$.pipe(
      switchMap(selectedCalendarId => this.databaseService.database.fetch.calendar(selectedCalendarId))
    )
  }

  private setTerms$() {
    if (this.terms$) return
    this.terms$ = this.selectedCalendarId$.pipe(
      switchMap(selectedCalendarId => {
        return this.popupService.runWithPopup(
          'Fetching terms',
          of(undefined).pipe(
            switchMap(() => {
              if (selectedCalendarId) return this.databaseService.database.all.terms(selectedCalendarId)
              return this.getTermsFromAllCalendars()
            }),
            map(terms => {
              this.hasTerms = terms.length != 0
              return terms
            })
          )
        )
      })
    )
  }

  private getTermsFromAllCalendars(): Observable<Term[]> {
    return this.databaseService.database.all.calendars(this.databaseService.accountId).pipe(
      switchMap(calendars => of(...calendars).pipe(
        concatMap(calendar => {
          return this.databaseService.database.all.terms(calendar._id).pipe(
            map(terms => terms.map(term => ({ ...term, calendar: calendar })))
          )
        }),
        concatMap(terms => of(...terms)), // create a strem of terms (gathered from each calendar)
        toArray()
      ))
    )
  }

  async goToTerm(term?: Term) {
    await this.selectedCalendarId$.pipe(
      take(1),
      map(selectedCalendarId => this.pushViewEvent.emit({
        name: ViewName.TERM,
        docId: term?._id,
        parentId: (<Calendar>term?.calendar)?._id || selectedCalendarId
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
