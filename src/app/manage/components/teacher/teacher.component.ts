import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { FormGroup, FormControl, Validators } from '@angular/forms'
import { ActivatedRoute } from '@angular/router'
import { ObjectId } from 'bson'
import { Observable, of } from 'rxjs'
import { map, switchMap, take } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Course } from 'src/app/model/course'
import { Teacher } from 'src/app/model/teacher'
import { EMAIL_SCHEMA_REGEX, NAME_REGEX } from 'src/app/model/_model'
import { ErrorCodes } from 'src/app/services/ErrorCodes'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'

@Component({
  selector: 'manage-teacher',
  templateUrl: './teacher.component.html',
  styleUrls: ['../../manage.component.css']
})
export class TeacherComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  @Output() popViewEvent = new EventEmitter<void>()
  teacherId$: Observable<Teacher['_id']>
  teacher$: Observable<Teacher>
  form: FormGroup

  constructor(
    private activatedRoute: ActivatedRoute,
    private databaseService: DatabaseService,
    private popupService: PopupService
  ) { }

  ngOnInit() {
    this.teacherId$ = this.activatedRoute.queryParamMap.pipe(
      map(queryParams => queryParams.get('docId'))
    )
    this.teacher$ = this.teacherId$.pipe(
      switchMap(teacherId => {
        if (!teacherId) return of(null)
        return this.popupService.runWithPopup('Fetching teacher', this.databaseService.database.fetch.teacher(teacherId))
      }),
      map(teacher => this.setForm(teacher))
    )
  }

  setForm(teacher?: Teacher): Teacher {
    const initialState = {
      firstName: teacher ? teacher.firstName : '',
      lastName: teacher ? teacher.lastName : '',
      email: teacher ? teacher.email : ''
    }
    this.form = new FormGroup({
      firstName: new FormControl(initialState.firstName, [Validators.required, Validators.pattern(NAME_REGEX)]),
      lastName: new FormControl(initialState.lastName, [Validators.required, Validators.pattern(NAME_REGEX)]),
      email: new FormControl(initialState.email, [Validators.email, Validators.pattern(EMAIL_SCHEMA_REGEX)])
    })
    return teacher
  }

  async submit(): Promise<void> {
    await this.popupService.runWithPopup(
      'Saving teacher',
      this.teacherId$.pipe(
        take(1),
        switchMap(teacherId => {
          const teacher: Teacher = {
            _id: teacherId || new ObjectId().toHexString(),
            account: this.databaseService.accountId,
            firstName: this.form.get('firstName').value,
            lastName: this.form.get('lastName').value
          }
          const email = this.form.get('email').value
          if (email) teacher.email = email
          return this.databaseService.database.put.teacher(teacher).pipe(
            map(() => this.pushViewEvent.emit({
              name: ViewName.TEACHER,
              docId: teacher._id,
              replacesUrl: true
            }))
          )
        }),
      ),
      ErrorCodes.ERR_TEACHER_EXISTS
    ).toPromise()
  }

  async removeTeacher(teacher: Teacher) {
    if (!confirm('Are you sure you want to remove this teacher?')) return
    await this.popupService
      .runWithPopup(
        'Removing teacher',
        this.databaseService.database.remove.teacher(teacher._id).pipe(
          map(() => this.popViewEvent.emit())
        )
      )
      .toPromise()
  }

  goToCourse(course?: Course) {
    this.pushViewEvent.emit({
      name: ViewName.COURSE,
      docId: course?._id,
      parentId: <Teacher['_id']>course?.teacher,
      replacesUrl: true
    })
  }
}
