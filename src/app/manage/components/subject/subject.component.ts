import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { FormControl, FormGroup, Validators } from '@angular/forms'
import { Observable, of } from 'rxjs'
import { map, switchMap, take } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { PopupService } from 'src/app/services/popup.service'
import { Subject } from 'src/app/model/subject'
import { ObjectId } from 'bson'
import { ErrorCodes } from 'src/app/services/ErrorCodes'
import { ViewInfo, ViewName } from '../../manage.component'
import { Hue } from 'src/app/model/hue'
import { UtilService } from 'src/app/services/util.service'
import { CODE_REGEX, TITLE_REGEX } from 'src/app/model/_model'

@Component({
  selector: 'manage-subject',
  templateUrl: './subject.component.html',
  styleUrls: ['../../manage.component.css']
})
export class SubjectComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  @Output() popViewEvent = new EventEmitter<void>()
  subjectId$: Observable<Subject['_id']>
  subject$: Observable<Subject>
  form: FormGroup

  constructor(
    private activatedRoute: ActivatedRoute,
    private databaseService: DatabaseService,
    private popupService: PopupService,
    private util: UtilService
  ) { }

  ngOnInit() {
    this.subjectId$ = this.activatedRoute.queryParamMap.pipe(map(queryParams => queryParams.get('docId')))
    this.subject$ = this.subjectId$.pipe(
      switchMap(subjectId => {
        if (!subjectId) return of(null)
        return this.popupService.runWithPopup('Fetching subject', this.databaseService.database.fetch.subject(subjectId))
      }),
      map(subject => this.setForm(subject))
    )
  }

  private setForm(subject?: Subject): Subject {
    const initialState = {
      code: subject ? subject.code : '',
      name: subject ? subject.name : '',
      hue: subject ? subject.hue : Hue.TEAL
    }
    this.form = new FormGroup({
      code: new FormControl(initialState.code, [Validators.required, Validators.pattern(CODE_REGEX)]),
      name: new FormControl(initialState.name, [Validators.required, Validators.pattern(TITLE_REGEX)]),
      hue: new FormControl(initialState.hue, [Validators.required, this.util.getHueValidator()])
    })
    return subject
  }

  async submit() {
    await this.popupService
      .runWithPopup(
        'Saving subject',
        this.subjectId$.pipe(
          take(1),
          switchMap(subjectId => {
            const subject: Subject = {
              _id: subjectId || new ObjectId().toHexString(),
              account: this.databaseService.accountId,
              code: this.form.get('code').value,
              name: this.form.get('name').value,
              hue: this.form.get('hue').value
            }
            return this.databaseService.database.put.subject(subject).pipe(
              map(() => subject._id)
            )
          }),
          map(docId => {
            this.pushViewEvent.emit({
              name: ViewName.SUBJECT,
              docId,
              replacesUrl: true
            })
          })
        ),
        ErrorCodes.ERR_SUBJECT_EXISTS
      )
      .toPromise()
  }

  async removeSubject(subject: Subject) {
    if (!confirm('Are you sure you want to remove this subject?')) return
    await this.popupService
      .runWithPopup('Removing subject', this.databaseService.database.remove.subject(subject._id).pipe(map(() => this.popViewEvent.emit())))
      .toPromise()
  }
}
