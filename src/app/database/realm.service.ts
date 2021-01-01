import { Injectable } from '@angular/core';
import * as Realm from "realm-web";
import { DatabaseLink } from "./databaseLink";
import { environment } from "../../environments/environment"
import { Subject } from '../model/subject';
import { Account } from '../model/account';
import { Calendar } from '../model/calendar';
import { Class } from '../model/class';
import { Course } from '../model/course';
import { Deliverable } from '../model/deliverable';
import { Teacher } from '../model/teacher';
import { Term } from '../model/term';
import { Test } from '../model/test';
import { ObjectId } from 'bson';
import { Model } from '../model/_model';
import { PopupService } from '../services/popup.service';
// import { AuthService } from '@auth0/auth0-angular';

@Injectable({
  providedIn: 'root'
})
export class RealmService implements DatabaseLink {
  private app: Realm.App
  private accountId: ObjectId

  constructor(
    private popupService: PopupService
  ) {
    console.log('RealmService: Creating a new App instance')
    this.app = new Realm.App({ id: environment.REALM_APP_ID });
    if (this.isLoggedIn()) this.accountId = new ObjectId(this.getLoggedInUser().id)
  }

  isLoggedIn() {
    //TODO add backups
    //TODO add log out
    //TODO allow blocking cross-origin cookies (possible?)
    return !!this.app.currentUser
  }

  getLoggedInUser() {
    return this.app.currentUser
  }

  async login() {
    console.log('RealmService: Logging in...')
    // const credentials = Realm.Credentials.google(environment.REALM_REDIRECT_URL)
    const credentials = Realm.Credentials.anonymous()
    const user = await this.app.logIn(credentials)
    this.accountId = new ObjectId(user.id)
  }

  async logout() {
    await this.app.currentUser.logOut()
  }

  private convertIdStringsToObjectIds<T extends Model>(model: T): T {
    const converted: T = {
      ...model,
      _id: new ObjectId(model._id),
      account: new ObjectId(`${model.account}`)
    }
    if ('calendar' in model) converted['calendar'] = new ObjectId(model['calendar'])
    if ('term' in model) converted['term'] = new ObjectId(model['term'])
    if ('course' in model) converted['course'] = new ObjectId(model['course'])
    if ('subject' in model) converted['subject'] = new ObjectId(model['subject'])
    if ('teacher' in model) converted['teacher'] = new ObjectId(model['teacher'])
    return converted
  }

  fetch = {
    calendar: (_id: Calendar['_id']) => this.popupService.performWithPopup(
      'Fetching calendar',
      () => this.app.currentUser.functions.fetchCalendar(new ObjectId(_id))
    ),
    subject: (_id: Subject['_id']) => this.popupService.performWithPopup(
      'Fetching subject',
      () => this.app.currentUser.functions.fetchSubject(new ObjectId(_id))
    ),
    term: (_id: Term['_id']) => this.popupService.performWithPopup(
      'Fetching term',
      () => this.app.currentUser.functions.fetchTerm(new ObjectId(_id))
    ),
    teacher: (_id: Teacher['_id']) => this.popupService.performWithPopup(
      'Fetching teacher',
      () => this.app.currentUser.functions.fetchTeacher(new ObjectId(_id))
    ),
    course: (_id: Course['_id']) => this.popupService.performWithPopup(
      'Fetching course',
      () => this.app.currentUser.functions.fetchCourse(new ObjectId(_id))
    ),
    class: (_id: Class['_id']) => this.popupService.performWithPopup(
      'Fetching class',
      () => this.app.currentUser.functions.fetchClass(new ObjectId(_id))
    ),
    deliverable: (_id: Deliverable['_id']) => this.popupService.performWithPopup(
      'Fetching deliverable',
      () => this.app.currentUser.functions.fetchDeliverable(new ObjectId(_id))
    ),
    test: (_id: Test['_id']) => this.popupService.performWithPopup(
      'Fetching test',
      () => this.app.currentUser.functions.fetchTest(new ObjectId(_id)
      ))
  }
  put = {
    // TODO ensure all account documents have unique emails
    // TODO rename Atlas DB to prod before launching (separate server for dev & prod)
    calendar: (calendar: Calendar) => this.app.currentUser.functions.putCalendar(
      this.convertIdStringsToObjectIds(calendar)
    ),
    subject: (subject: Subject) => this.app.currentUser.functions.putSubject(
      this.convertIdStringsToObjectIds(subject)
    ),
    term: (term: Term) => this.app.currentUser.functions.putTerm(
      this.convertIdStringsToObjectIds(term)
    ),
    teacher: (teacher: Teacher) => this.app.currentUser.functions.putTeacher(
      this.convertIdStringsToObjectIds(teacher)
    ),
    course: (course: Course) => this.app.currentUser.functions.putCourse(
      this.convertIdStringsToObjectIds(course)
    ),
    class: (klass: Class) => this.app.currentUser.functions.putClass(
      this.convertIdStringsToObjectIds(klass)
    ),
    deliverable: (deliverable: Deliverable) => this.app.currentUser.functions.putDeliverable(
      this.convertIdStringsToObjectIds(deliverable)
    ),
    test: (test: Test) => this.app.currentUser.functions.putTest(
      this.convertIdStringsToObjectIds(test)
    )
  }
  all = {
    calendars: (accountId: Account['_id']) => this.popupService.performWithPopup(
      'Fetching calendars',
      () => this.app.currentUser.functions.fetchCalendars(new ObjectId(accountId))),
    subjects: (accountId: Account['_id']) => this.popupService.performWithPopup(
      'Fetching subjects',
      () => this.app.currentUser.functions.fetchSubjects(new ObjectId(accountId))),
    terms: (calendarId: Calendar['_id']) => this.popupService.performWithPopup(
      'Fetching terms',
      () => this.app.currentUser.functions.fetchTerms(new ObjectId(calendarId))),
    teachers: (calendarId: Calendar['_id']) => this.popupService.performWithPopup(
      'Fetching teachers',
      () => this.app.currentUser.functions.fetchTeachers(new ObjectId(calendarId))),
    courses: (termId: Term['_id']) => this.popupService.performWithPopup(
      'Fetching courses',
      () => this.app.currentUser.functions.fetchCourses(new ObjectId(termId))),
    classes: (courseId: Course['_id']) => this.popupService.performWithPopup(
      'Fetching classes',
      () => this.app.currentUser.functions.fetchClasses(new ObjectId(courseId))),
    deliverables: (courseId: Course['_id']) => this.popupService.performWithPopup(
      'Fetching deliverables',
      () => this.app.currentUser.functions.fetchDeliverables(new ObjectId(courseId))),
    tests: (courseId: Course['_id']) => this.popupService.performWithPopup(
      'Fetching tests',
      () => this.app.currentUser.functions.fetchTests(new ObjectId(courseId)))
  }
  remove = {
    calendar: async (_id: Calendar['_id']) => this.app.currentUser.functions.removeCalendar(new ObjectId(_id)),
    subject: async (_id: Subject['_id']) => this.app.currentUser.functions.removeSubject(new ObjectId(_id)),
    term: async (_id: Term['_id']) => this.app.currentUser.functions.removeTerm(new ObjectId(_id)),
    teacher: async (_id: Teacher['_id']) => this.app.currentUser.functions.removeTeacher(new ObjectId(_id)),
    course: async (_id: Course['_id']) => this.app.currentUser.functions.removeCourse(new ObjectId(_id)),
    class: async (_id: Class['_id']) => this.app.currentUser.functions.removeClass(new ObjectId(_id)),
    deliverable: async (_id: Deliverable['_id']) => this.app.currentUser.functions.removeDeliverable(new ObjectId(_id)),
    test: async (_id: Test['_id']) => this.app.currentUser.functions.removeTest(new ObjectId(_id))
  }
  search = {
    classesWithinRange: (min: Date, max: Date) => this.popupService.performWithPopup(
      'Searching for classes',
      () => this.app.currentUser.functions.searchForClassesWithinRange(min.getTime(), max.getTime())),
    deliverablesDueWithinRange: (min: Date, max: Date) => this.popupService.performWithPopup(
      'Searching for deliverables',
      () => this.app.currentUser.functions.searchForDeliverablesDueWithinRange(min.getTime(), max.getTime())),
    testsWithinRange: (min: Date, max: Date) => this.popupService.performWithPopup(
      'Searching for tests',
      () => this.app.currentUser.functions.searchForTestsWithinRange(min.getTime(), max.getTime()))
  }
}
