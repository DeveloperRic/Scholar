import { NgModule } from '@angular/core'
import { ScholarCommonModule } from '../scholar-common/scholar-common.module'
import { CalendarComponent } from './components/calendar/calendar.component'
import { CalendarsComponent } from './components/calendars/calendars.component'
import { ClassesComponent } from './components/classes/classes.component'
import { CoursesComponent } from './components/courses/courses.component'
import { DeliverablesComponent } from './components/deliverables/deliverables.component'
import { FilterMenu } from './components/filter-menu/filter-menu.component'
import { IndexComponent } from './components/index/index.component'
import { SubjectComponent } from './components/subject/subject.component'
import { SubjectsComponent } from './components/subjects/subjects.component'
import { TeacherComponent } from './components/teacher/teacher.component'
import { TeachersComponent } from './components/teachers/teachers.component'
import { TermComponent } from './components/term/term.component'
import { TermsComponent } from './components/terms/terms.component'
import { TestsComponent } from './components/tests/tests.component'
import { ManageRoutingModule } from './manage-routing.module'
import { ManageComponent } from './manage.component'

@NgModule({
  declarations: [
    ManageComponent,
    IndexComponent,
    CalendarsComponent,
    CalendarComponent,
    ClassesComponent,
    CoursesComponent,
    DeliverablesComponent,
    SubjectsComponent,
    SubjectComponent,
    TeachersComponent,
    TeacherComponent,
    TermsComponent,
    TermComponent,
    TestsComponent,
    FilterMenu
  ],
  imports: [ManageRoutingModule, ScholarCommonModule],
  bootstrap: [ManageComponent]
})
export class ManageModule { }
