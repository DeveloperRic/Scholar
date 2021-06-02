import { Component, OnInit, Input, Output, EventEmitter, OnDestroy } from '@angular/core'
import { FormGroup, FormControl, Validators } from '@angular/forms'
import { Observable, Subscription } from 'rxjs'
import { DatabaseService } from 'src/app/database/database.service'
import { Calendar } from 'src/app/model/calendar'
import { ID_REGEX } from '../../manage.component'

export enum FilterField {
  CALENDAR,
  TERM
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

  constructor(private databaseService: DatabaseService) { }

  ngOnInit(): void {
    this.calendars$ = this.databaseService.database.all.calendars(this.databaseService.accountId)
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
    this.form = new FormGroup(controls)
    this.formChangeSubscription = this.form.valueChanges.subscribe(this.formChangeEvent)
  }

  filterIsEnabled(field: FilterField) {
    return this.enabledFilters.includes(field)
  }
}
