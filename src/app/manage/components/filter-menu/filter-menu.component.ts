import { Component, Input, OnInit } from '@angular/core'
import { FormControl, FormGroup, Validators } from '@angular/forms'
import { Observable } from 'rxjs'

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
  styles: ['./manage-filter-menu.component.css']
})
export class FilterMenu implements OnInit {
  @Input() enabledFilters: FilterField[]
  FilterField = FilterField
  form: FormGroup
  calendars$: Observable<Calendar[]> = this.getCalendars()

  constructor(private databaseService: DatabaseService) {}

  ngOnInit(): void {
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

  private setForm() {
    const controls: { [name: string]: FormControl } = {}
    if (this.filterIsEnabled(FilterField.CALENDAR)) {
      controls.calendar = new FormControl('', [Validators.required, Validators.pattern(ID_REGEX)])
    }
    this.form = new FormGroup(controls)
  }

  filterIsEnabled(field: FilterField) {
    return this.enabledFilters.includes(field)
  }

  getCalendars() {
    return this.databaseService.database.all.calendars(this.databaseService.accountId)
  }
}
