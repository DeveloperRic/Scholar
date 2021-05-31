import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'
import { Observable } from 'rxjs'
import { map } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { FilterField } from 'src/app/manage/components/filter-menu/filter-menu.component'
import { Teacher } from 'src/app/model/teacher'
import { PopupService } from 'src/app/services/popup.service'

@Component({
  selector: 'manage-teachers',
  templateUrl: './teachers.component.html'
})
export class TeachersComponent implements OnInit {
  teachers$: Observable<Teacher[]>
  noTeachers: boolean
  filterMenuVisible: boolean
  enabledFilters = [FilterField.CALENDAR, FilterField.TERM]

  constructor(private router: Router, private databaseService: DatabaseService, private popupService: PopupService) {}

  ngOnInit() {
    this.teachers$ = this.popupService.runWithPopup(
      'Fetching teachers',
      this.databaseService.database.all.teachers(this.databaseService.accountId).pipe(
        map(teachers => {
          this.noTeachers = teachers.length === 0
          return teachers
        })
      )
    )
  }

  onAddTeacherClick() {
    // TODO navigateToView(this.router, ViewType.TERM)
  }

  toggleFilterMenu() {
    this.filterMenuVisible = !this.filterMenuVisible
  }
}
