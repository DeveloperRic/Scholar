import { Component, OnInit } from '@angular/core'
import { FormGroup } from '@angular/forms'
import { ActivatedRoute, Router } from '@angular/router'
import { ReplaySubject } from 'rxjs'
import { map, take } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Model } from 'src/app/model/_model'
import { ErrorCodes } from 'src/app/services/ErrorCodes'
import { PopupService } from 'src/app/services/popup.service'

export enum ViewName {
  INDEX = 'index',
  CALENDAR = 'calendar',
  CALENDARS = 'calendars',
  TERM = 'term',
  COURSE = 'course',
  CLASS = 'class',
  TEST = 'test',
  DELIVERABLE = 'deliverable',
  SUBJECT = 'subject',
  TEACHER = 'teacher'
}

export const ID_REGEX = /^[a-f\d]{24}$/i

export interface ViewInfo {
  name: ViewName
  parentId?: Model['_id']
  docId?: Model['_id']
  /** if true, replaces the current history entry */
  replacesUrl?: boolean
}

//TODO each view should be a separate component
export interface View {
  name: ViewName | '',
  parentId?: Model['_id'],
  list?: Model[],
  model?: Model,
  form?: FormGroup,
  submit?: (form: FormGroup) => void
}

@Component({
  selector: 'app-manage',
  templateUrl: './manage.component.html',
  styleUrls: ['./manage.component.css']
})
export class ManageComponent implements OnInit {
  private viewHistory: ViewInfo[] = []
  databaseStatus = 'loading'
  loading = false
  viewInfo$ = new ReplaySubject<ViewInfo>(1)
  canNavigateBack: boolean

  constructor(
    private activatedRoute: ActivatedRoute,
    private databaseService: DatabaseService,
    public popupService: PopupService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.init()
  }

  private async init() {
    console.log('Manage: Initialising...')
    try {
      await this.databaseService.init()
      await this.onDatabaseInitSuccess()
    } catch (err) {
      this.onDatabaseInitFail(err)
    }
  }

  private async onDatabaseInitSuccess() {
    try {
      this.databaseStatus = 'connected'
      this.viewInfo$.next(undefined) // the current view is queried when pushing views
      this.activatedRoute.queryParamMap.subscribe(queryParams => {
        this.pushView({
          name: <ViewName>queryParams.get('view') || ViewName.INDEX,
          parentId: queryParams.get('parentId'),
          docId: queryParams.get('docId')
        })
      })
      console.log('Manage: Done initialising')
    } catch (err) {
      this.popupService.newPopup({
        type: 'error',
        message: 'Manage: Error: initView() failed',
        error: err
      })
    }
  }

  private onDatabaseInitFail(err: { message: ErrorCodes }) {
    if (err.message == ErrorCodes.ERR_NOT_LOGGED_IN) {
      console.log('Manage: Not logged in, displaying login page')
      this.databaseStatus = 'login'
    } else {
      console.error('Manage: Error: Init failed', err)
      this.databaseStatus = 'failed'
    }
  }

  private async pushView(newView: ViewInfo) {
    await this.viewInfo$.pipe(
      take(1),
      map(currentView => {
        if (currentView) this.viewHistory.push(currentView)
        this.canNavigateBack = newView.name != ViewName.INDEX && this.viewHistory.length != 0
        this.viewInfo$.next(newView)
      })
    ).toPromise()
  }

  updateViewQueryParams(view: ViewInfo) {
    this.router.navigate(
      [],
      {
        queryParams: {
          view: view.name == ViewName.INDEX ? null : view.name,
          parentId: view.parentId || null,
          docId: view.docId || null
        },
        replaceUrl: view.replacesUrl
      }
    )
  }

  popView() {
    if (this.viewHistory.length == 0) return
    this.viewHistory.pop()
    history.back()
  }

}
