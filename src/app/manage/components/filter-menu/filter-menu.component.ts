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

type Term = _Term & { calendar: Calendar }
type Course = _Course & { subject: Subject }

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
        return of(...terms).pipe(
          concatMap(term => this.databaseService.database.all.courses(term._id).pipe(
            switchMap(courses => of(...courses)),
            switchMap(course => {
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
    // const form = new FormGroup({
    //   code: new FormControl(initialState.code, [Validators.required, Validators.pattern(CODE_REGEX)]),
    //   name: new FormControl(initialState.name, [Validators.required, Validators.pattern(TITLE_REGEX)]),
    //   subject: ,
    //   teacher: new FormControl(initialState.teacher, Validators.pattern(/^[a-f\d]{24}$/i))
    // })
    // courseView.form = form
    // courseView.submit = async () => {
    //   const course: Course = {
    //     _id: courseId || new ObjectId().toHexString(),
    //     account: this.databaseService.accountId,
    //     term: termId,
    //     subject: form.get('subject').value,
    //     code: form.get('code').value,
    //     name: form.get('name').value
    //   }
    //   const teacher = form.get('teacher').value
    //   if (teacher) course.teacher = teacher
    //   this.popupService.performWithPopup(
    //     'Saving course',
    //     () => this.database.put.course(course),
    //     ErrorCodes.ERR_COURSE_EXISTS)
    //     .then(() => this.navigateToView(ViewType.TERM, termId))
    // }
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
