import { Component, OnInit, Input, Output, EventEmitter, OnDestroy } from '@angular/core'
import { FormGroup, FormControl, Validators } from '@angular/forms'
import { Observable, of, Subscription } from 'rxjs'
import { concatMap, map, shareReplay, switchMap, toArray } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Calendar } from 'src/app/model/calendar'
import { Course as _Course } from 'src/app/model/course'
import { Subject } from 'src/app/model/subject'
import { Term as _Term } from 'src/app/model/term'
import { ID_REGEX } from 'src/app/model/_model'

type Term = Omit<_Term, 'calendar'> & { calendar: Calendar }
type Course = Omit<_Course, 'subject'> & { subject: Subject }

export enum FilterField {
  CALENDAR,
  TERM,
  COURSE
}

@Component({
  selector: 'manage-filter-menu',
  templateUrl: './filter-menu.component.html',
  styles: ['./filter-menu.component.css']
})
export class FilterMenu implements OnInit, OnDestroy {
  @Input() enabledFilters: FilterField[]
  @Output() formChangeEvent = new EventEmitter()
  formChangeSubscription: Subscription
  FilterField = FilterField
  form: FormGroup
  calendars$: Observable<Calendar[]>
  terms$: Observable<Term[]>
  courses$: Observable<Course[]>

  constructor(private databaseService: DatabaseService) { }

  ngOnInit(): void {
    this.calendars$ = this.databaseService.database.all.calendars(this.databaseService.accountId).pipe(
      shareReplay(1)
    )
    this.terms$ = this.calendars$.pipe(
      switchMap(calendars => {
        // this of(...) could be in a separate operator above but it has to be here because toArray() requires
        // pipe completion which calendars$ will not provide until the very end (indefinite end)
        return of(...calendars).pipe(
          concatMap(calendar => this.databaseService.database.all.terms(calendar._id).pipe(
            switchMap(terms => of(...terms)),
            map(term => ({ ...term, calendar }))
          )),
          toArray()
        )
      })
    )
    this.courses$ = this.terms$.pipe(
      switchMap(terms => {
        // this of(...) could be in a separate operator above but it has to be here because toArray() requires
        // pipe completion which terms$ will not provide until the very end (indefinite end)
        return of(...terms).pipe(
          concatMap(term => this.databaseService.database.all.courses(term._id).pipe(
            switchMap(courses => of(...courses)),
            concatMap(course => {
              return this.databaseService.database.fetch.subject(<Subject['_id']>course.subject).pipe(
                map(subject => {
                  return <Course>{ ...course, subject }
                })
              )
            })
          )),
          toArray()
        )
      })
    )
    this.setForm()
  }

  ngOnDestroy() {
    if (this.formChangeSubscription) this.formChangeSubscription.unsubscribe()
  }

  private setForm() {
    const controls: { [name: string]: FormControl } = {}
    if (this.filterIsEnabled(FilterField.CALENDAR)) {
      controls.calendar = new FormControl('', [Validators.required, Validators.pattern(ID_REGEX)])
    }
    if (this.filterIsEnabled(FilterField.TERM)) {
      controls.term = new FormControl('', [Validators.required, Validators.pattern(ID_REGEX)])
    }
    if (this.filterIsEnabled(FilterField.COURSE)) {
      controls.course = new FormControl('', [Validators.required, Validators.pattern(ID_REGEX)])
    }
    this.form = new FormGroup(controls)
    this.formChangeSubscription = this.form.valueChanges.subscribe(this.formChangeEvent)
  }

  filterIsEnabled(field: FilterField) {
    return this.enabledFilters.includes(field)
  }
}
