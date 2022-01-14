import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { FormGroup, FormControl, Validators, FormArray } from '@angular/forms'
import { from, Observable } from 'rxjs'
import { map, shareReplay, switchMap, tap } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Course as _Course } from 'src/app/model/course'
import { PopupService } from 'src/app/services/popup.service'
import { ViewInfo, ViewName } from '../../manage.component'
import { UtilService } from 'src/app/services/util.service'
import { Account, GradingSettings } from 'src/app/model/account'

type GpaScaleItem = GradingSettings['gpaScale'][0]

@Component({
  selector: 'manage-account',
  templateUrl: './account.component.html',
  styleUrls: [ '../../manage.component.css' ],
})
export class AccountComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()
  @Output() popViewEvent = new EventEmitter<void>()
  gradingForm$: Observable<FormGroup>

  constructor(
    private databaseService: DatabaseService,
    private popupService: PopupService,
    private util: UtilService,
  ) { }

  ngOnInit() {
    this.gradingForm$ = from(
      this.databaseService.database.fetch.account(this.databaseService.accountId),
    ).pipe(
      map(account => {
        const initialState = {
          numCoursesNotInScholar: account?.settings?.grading?.gpaHistory?.numCoursesNotInScholar || 0,
          cumulativeGpaBeforeScholar: account?.settings?.grading?.gpaHistory?.cumulativeGpaBeforeScholar || 0,
          gpaScale: account?.settings?.grading?.gpaScale || [],
        }
        return new FormGroup({
          numCoursesNotInScholar: new FormControl(initialState.numCoursesNotInScholar, [ Validators.required, Validators.min(0) ]),
          cumulativeGpaBeforeScholar: new FormControl(
            initialState.cumulativeGpaBeforeScholar,
            [ Validators.required, Validators.min(0), Validators.max(100) ],
          ),
          gpaScale: new FormArray(
            initialState.gpaScale.map(item => new FormGroup({
              minPercent: new FormControl(item.minPercent),
              code: new FormControl(item.code),
              maxPercent: new FormControl(item.maxPercent),
            })),
            array => this.validateGpaScale(array as FormArray),
          ),
        })
      }),
      shareReplay({ bufferSize: 1, refCount: true }),
    )
  }

  private extractGpaScale(array: FormArray): GpaScaleItem[] {
    const controls = array.controls as FormGroup[]
    return controls.map(group => {
      return {
        minPercent: group.get('minPercent').value,
        code: group.get('code').value,
        maxPercent: group.get('maxPercent').value,
      } as GpaScaleItem
    })
  }

  validateGpaScale(array: FormArray) {
    const errors = {}
    const scale = this.extractGpaScale(array)
      .sort((a, b) => a.minPercent - b.minPercent) // asc order of minPercent
    if (scale.length == 0) return
    if (scale[0].minPercent != 0) {
      errors['badFirstElement'] = 'First item must start at 0'
    }
    if (scale[scale.length - 1].maxPercent != 100) {
      errors['badLastElement'] = 'Last item must end at 100'
    }
    scale.slice(1).reduce((prev, cur) => {
      if (prev.maxPercent != cur.minPercent) {
        errors[`badMinPercent=${cur.minPercent}`] = 'minPercents must equal the previous maxPercent'
      }
      return cur
    }, scale[0])
    if (Object.keys(errors).length != 0) {
      return errors
    }
  }

  async submit(): Promise<void> {
    await this.popupService.runWithPopup(
      'Saving account',
      this.gradingForm$.pipe(
        switchMap(gradingForm => {
          const account: Account = {
            _id: this.databaseService.accountId, // accounts col is out of sync with users col, so must be explicit
            account: this.databaseService.accountId,
            settings: {
              grading: {
                gpaHistory: {
                  numCoursesNotInScholar: gradingForm.get('numCoursesNotInScholar').value,
                  cumulativeGpaBeforeScholar: gradingForm.get('cumulativeGpaBeforeScholar').value,
                },
                gpaScale: this.extractGpaScale(gradingForm.get('gpaScale') as FormArray),
              },
            },
          }
          return this.databaseService.database.put.account(account).pipe(
            map(() => this.pushViewEvent.emit({
              name: ViewName.ACCOUNT,
              replacesUrl: true,
            })),
          )
        }),
      ),
    ).toPromise()
  }

  async addGpaScaleItem() {
    await this.gradingForm$.pipe(
      tap(gradingForm => {
        const array = gradingForm.get('gpaScale') as FormArray
        const scale = this.extractGpaScale(array)
        const nextItem = new FormGroup({
          minPercent: new FormControl(scale[scale.length - 1]?.minPercent || 0),
          code: new FormControl('?'),
          maxPercent: new FormControl(scale[scale.length - 1]?.maxPercent || 100),
        })
        array.push(nextItem)
      }),
    ).toPromise()
  }

  async removeGpaScaleItem(index: number) {
    await this.gradingForm$.pipe(
      tap(gradingForm => {
        const array = gradingForm.get('gpaScale') as FormArray
        array.removeAt(index)
      }),
    ).toPromise()
  }
}
