import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'
import { Observable } from 'rxjs'
import { map } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Course } from 'src/app/model/course'
import { PopupService } from 'src/app/services/popup.service'

@Component({
  selector: 'manage-courses',
  templateUrl: './courses.component.html'
})
export class CoursesComponent implements OnInit {
  courses$: Observable<Course[]>
  noCourses: boolean

  constructor(private router: Router, private databaseService: DatabaseService, private popupService: PopupService) {}

  ngOnInit() {
    this.courses$ = this.popupService.runWithPopup(
      'Fetching courses',
      this.databaseService.database.all.courses(this.databaseService.accountId).pipe(
        map(courses => {
          this.noCourses = courses.length === 0
          return courses
        })
      )
    )
  }

  onAddCourseClick() {
    // TODO navigateToView(this.router, ViewType.TERM)
  }
}
