import { Component, OnInit, Output } from '@angular/core';
import { Location as NgLocation } from '@angular/common'
import { FormControl, FormGroup, ValidatorFn, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UtilService } from '../../services/util.service';
import { ErrorCodes } from "../../services/ErrorCodes";
import { DatabaseService } from '../../database/database.service';
import { PopupService } from '../../services/popup.service';
import { ObjectId } from 'bson';
import { Calendar } from '../../model/calendar';
import { Class } from '../../model/class';
import { Course } from '../../model/course';
import { Deliverable } from '../../model/deliverable';
import { Hue } from '../../model/hue';
import { Location } from '../../model/location';
import { RelativeWeeklyScheduleRepeat, ScheduleRepeatBasis, ScheduleRepeatDay } from '../../model/schedule';
import { Subject } from '../../model/subject';
import { Teacher } from '../../model/teacher';
import { Term } from '../../model/term';
import { Test } from '../../model/test';
import { Model } from '../../model/_model';
import { DatabaseLink } from '../../database/databaseLink';

//TODO each view should be a separate component
interface View {
  name: ViewType | '',
  parentId?: Model['_id'],
  list?: Model[],
  model?: Model,
  form?: FormGroup,
  submit?: (form: FormGroup) => void
}

interface CalendarListView extends View {
  subjects: Subject[]
}

interface CalendarView extends View {
  model: Calendar
  teachers: Teacher[]
}

interface ClassView extends View {
  model: Class
  teachers: Teacher[]
}

interface CourseView extends View {
  model: Course
  subjects: Subject[]
  teachers: Teacher[]
  tests: Test[]
  deliverables: Deliverable[]
}

interface DeliverableView extends View {
  model: Deliverable
}

interface TestView extends View {
  model: Test
}

export enum ViewType {
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

const CODE_REGEX = /^\w[\w-]*$/
const TITLE_REGEX = /^\w[\w -]*$/
const NAME_REGEX = /^[^\s][^\n]*$/

@Component({
  selector: 'app-manage',
  templateUrl: './manage.component.html',
  styleUrls: ['./manage.component.css']
})
export class ManageComponent implements OnInit {
  private database: DatabaseLink
  @Output() databaseStatus = 'loading'
  @Output() loading = false
  @Output() view: View = { name: '' }

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private location: NgLocation,
    private util: UtilService,
    private databaseService: DatabaseService,
    private popupService: PopupService
  ) { }

  ngOnInit(): void {
    this.init()
  }

  private init() {
    console.log('Manage: Initialising...')
    this.databaseService.init()
      .then(database => {
        this.database = database
        return this.initView()
          .then(() => {
            this.databaseStatus = 'connected'
            console.log('Manage: Done initialising')
          })
          .catch(err => this.popupService.newPopup({
            type: 'error',
            message: 'Manage: Error: initView() failed',
            error: err
          }))
      })
      .catch(err => {
        if (err.message == ErrorCodes.ERR_NOT_LOGGED_IN) {
          console.log('Manage: Not logged in, displaying login page')
          this.databaseStatus = 'login'
        } else {
          console.error('Manage: Error: Init failed', err)
          this.databaseStatus = 'failed'
        }
      })
  }

  private initView() {
    return new Promise<void>((resolve, reject) => {
      this.activatedRoute.queryParams.subscribe(async params => {
        try {
          const view: View = {
            name: params['view'] || ViewType.CALENDARS,
            list: []
          }
          const modelId = params['_id']
          const parentId = params['parent']
          this.view = await this.setViewProperties(view, modelId, parentId)
          if (!this.view.parentId) this.view.parentId = parentId
          resolve()
        } catch (err) {
          reject(err)
        }
      });
    })
  }

  private async setViewProperties(view: View, modelId?: Model['_id'], parentId?: Model['_id']) {
    if (view.name == ViewType.CALENDARS) return await this.setCalendarListViewProperties(view)
    if (view.name == ViewType.CALENDAR) return await this.setCalendarViewProperties(view, modelId)
    if (view.name == ViewType.SUBJECT) return await this.setSubjectViewProperties(view, modelId)
    if (view.name == ViewType.TERM) return await this.setTermViewProperties(view, modelId, parentId)
    if (view.name == ViewType.TEACHER) return await this.setTeacherViewProperties(view, modelId, parentId)
    if (view.name == ViewType.COURSE) return await this.setCourseViewProperties(view, modelId, parentId)
    if (view.name == ViewType.CLASS) return await this.setClassViewProperties(view, modelId, parentId)
    if (view.name == ViewType.DELIVERABLE) return await this.setDeliverableViewProperties(view, modelId, parentId)
    if (view.name == ViewType.TEST) return await this.setTestViewProperties(view, modelId, parentId)
  }

  navigateToView(name: ViewType, modelId?: Model['_id'], parentId?: Model['_id']) {
    let url = `/manage?view=${name}`
    if (modelId) url += `&_id=${modelId}`
    if (parentId) url += `&parent=${parentId}`
    this.router.navigateByUrl(url, { replaceUrl: true })
  }

  goBack() {
    if (this.view.name == ViewType.CALENDARS) throw new Error('nowhere to go')
    if (this.view.name == ViewType.CALENDAR) this.navigateToView(ViewType.CALENDARS)
    if (this.view.name == ViewType.SUBJECT) this.navigateToView(ViewType.CALENDARS)
    if (this.view.name == ViewType.TERM) this.navigateToView(ViewType.CALENDAR, this.view.parentId)
    if (this.view.name == ViewType.TEACHER) this.navigateToView(ViewType.CALENDAR, this.view.parentId)
    if (this.view.name == ViewType.COURSE) this.navigateToView(ViewType.TERM, this.view.parentId)
    if (this.view.name == ViewType.CLASS) this.navigateToView(ViewType.COURSE, this.view.parentId)
    if (this.view.name == ViewType.DELIVERABLE) this.navigateToView(ViewType.COURSE, this.view.parentId)
    if (this.view.name == ViewType.TEST) this.navigateToView(ViewType.COURSE, this.view.parentId)
  }

  private getJSONValidator<T extends {}>(errorKey: string): ValidatorFn {
    return control => {
      const errors = {}
      if (control.value == '') return errors;
      try {
        const _obj: T = JSON.parse(control.value)
      } catch {
        errors[errorKey] = { value: control.value }
      }
      return errors;
    }
  }

  private getDateValidator(min?: Date, max?: Date): ValidatorFn {
    return control => {
      if (control.value == '') return {};
      try {
        const date = new Date(control.value)
        if (min && date < min || max && date > max) {
          return { badDate: { value: control.value } }
        }
      } catch {
        return { badDate: { value: control.value } }
      }
      return {};
    }
  }

  private getHueValidator(): ValidatorFn {
    return control => {
      if (control.value == '') return {};
      if (!Object.values(Hue).includes(control.value)) return { badHue: { value: control.value } }
      return {};
    }
  }

  private async setCalendarListViewProperties(view: View) {
    const calendarListView: CalendarListView = { ...view, subjects: [] }
    calendarListView.list.push(...await this.database.all.calendars(this.databaseService.accountId))
    calendarListView.subjects.push(...await this.database.all.subjects(this.databaseService.accountId))
    return calendarListView
  }

  private async setCalendarViewProperties(view: View, calendarId?: Calendar['_id']) {
    const calendarView: CalendarView = { ...view, model: null, teachers: [] }
    const currentYear = new Date().getFullYear()
    if (calendarId) {
      calendarView.model = await this.database.fetch.calendar(calendarId)
    }
    const initialState = calendarId ? calendarView.model.year : currentYear
    const form = new FormGroup({
      year: new FormControl(initialState, [Validators.required, Validators.min(currentYear - 1)])
    })
    calendarView.form = form
    calendarView.submit = async () => {
      const calendar: Calendar = {
        _id: calendarId || new ObjectId().toHexString(),
        account: this.databaseService.accountId,
        year: form.get('year').value
      }
      this.popupService.performWithPopup(
        'Saving calendar',
        () => this.database.put.calendar(calendar),
        ErrorCodes.ERR_CALENDAR_EXISTS)
        .then(() => this.navigateToView(ViewType.CALENDARS))
    }
    if (calendarId) {
      calendarView.list.push(...await this.database.all.terms(calendarId))
      calendarView.teachers.push(...await this.database.all.teachers(calendarId))
    }
    return calendarView
  }

  private async setSubjectViewProperties(view: View, subjectId?: Subject['_id']) {
    if (subjectId) view.model = await this.database.fetch.subject(subjectId)
    const initialState = {
      code: subjectId ? (<Subject>view.model).code : '',
      name: subjectId ? (<Subject>view.model).name : '',
      hue: subjectId ? (<Subject>view.model).hue : Hue.TEAL
    }
    const form = new FormGroup({
      code: new FormControl(initialState.code, [Validators.required, Validators.pattern(CODE_REGEX)]),
      name: new FormControl(initialState.name, [Validators.required, Validators.pattern(TITLE_REGEX)]),
      hue: new FormControl(initialState.hue, [Validators.required, this.getHueValidator()])
    })
    view.form = form
    view.submit = async () => {
      const subject: Subject = {
        _id: subjectId || new ObjectId().toHexString(),
        account: this.databaseService.accountId,
        code: form.get('code').value,
        name: form.get('name').value,
        hue: form.get('hue').value
      }
      this.popupService.performWithPopup(
        'Saving subject',
        () => this.database.put.subject(subject),
        ErrorCodes.ERR_SUBJECT_EXISTS)
        .then(() => this.navigateToView(ViewType.CALENDARS))
    }
    return view
  }

  private async setTermViewProperties(view: View, termId?: Term['_id'], calendarId?: Calendar['_id']) {
    const currentMonth = new Date().getMonth()
    if (termId) {
      const term = await this.database.fetch.term(termId)
      calendarId = `${term.calendar}`
      view.parentId = calendarId
      term.calendar = await this.database.fetch.calendar(calendarId)
      view.model = term
    }
    const calendar = termId ? (<Calendar>(<Term>view.model).calendar) : await this.database.fetch.calendar(calendarId)
    const autoTermName =
      currentMonth >= 8 && currentMonth <= 11 ? 'Fall' :
        (currentMonth >= 0 && currentMonth <= 3 ? 'Winter' : 'Summer')
    const autoStartDate = new Date(calendar.year, currentMonth, 1)
    const autoEndDate = new Date(calendar.year, currentMonth + 1, 0)
    const initialState = {
      name: termId ? (<Term>view.model).name : autoTermName,
      start: this.util.toHTMLDate(termId ? (<Term>view.model).start || autoStartDate : autoStartDate),
      end: this.util.toHTMLDate(termId ? (<Term>view.model).end || autoEndDate : autoEndDate)
    }
    const minDate = new Date(calendar.year, 0)
    const maxDate = new Date(calendar.year + 1, 11, 31)
    const form = new FormGroup({
      name: new FormControl(initialState.name, [Validators.required, Validators.pattern(TITLE_REGEX)]),
      start: new FormControl(initialState.start, [Validators.required, this.getDateValidator(minDate, maxDate)]),
      end: new FormControl(initialState.end, [Validators.required, this.getDateValidator(minDate, maxDate)])
    })
    view.form = form
    view.submit = async () => {
      const term: Term = {
        _id: termId || new ObjectId().toHexString(),
        account: this.databaseService.accountId,
        calendar: calendarId,
        name: form.get('name').value,
        start: this.util.fromHTMLDate(form.get('start').value).getTime(),
        end: this.util.fromHTMLDate(form.get('end').value).getTime()
      }
      this.popupService.performWithPopup(
        'Saving term',
        () => this.database.put.term(term),
        ErrorCodes.ERR_TERM_EXISTS)
        .then(() => this.navigateToView(ViewType.CALENDAR, calendarId))
    }
    if (termId) view.list.push(...await this.database.all.courses(termId))
    return view
  }

  private async setTeacherViewProperties(view: View, teacherId?: Teacher['_id'], calendarId?: Calendar['_id']) {
    if (teacherId) {
      const teacher = await this.database.fetch.teacher(teacherId)
      calendarId = `${teacher.calendar}`
      view.parentId = calendarId
      view.model = teacher
    }
    const initialState = {
      firstName: teacherId ? (<Teacher>view.model).firstName : '',
      lastName: teacherId ? (<Teacher>view.model).lastName : '',
      email: teacherId ? (<Teacher>view.model).email : ''
    }
    const form = new FormGroup({
      firstName: new FormControl(initialState.firstName, [Validators.required, Validators.pattern(NAME_REGEX)]),
      lastName: new FormControl(initialState.lastName, [Validators.required, Validators.pattern(NAME_REGEX)]),
      email: new FormControl(initialState.email, Validators.email)
    })
    view.form = form
    view.submit = async () => {
      const teacher: Teacher = {
        _id: teacherId || new ObjectId().toHexString(),
        account: this.databaseService.accountId,
        calendar: calendarId,
        firstName: form.get('firstName').value,
        lastName: form.get('lastName').value,
        email: form.get('email').value
      }
      this.popupService.performWithPopup(
        'Saving teacher',
        () => this.database.put.teacher(teacher),
        ErrorCodes.ERR_TEACHER_EXISTS)
        .then(() => this.navigateToView(ViewType.CALENDAR, calendarId))
    }
    return view
  }

  private async setCourseViewProperties(view: View, courseId?: Course['_id'], termId?: Term['_id']) {
    const courseView: CourseView = { ...view, model: null, subjects: [], teachers: [], tests: [], deliverables: [] }
    if (courseId) {
      const course = await this.database.fetch.course(courseId)
      course.subject = await this.database.fetch.subject(`${course.subject}`)
      termId = `${course.term}`
      courseView.parentId = termId
      courseView.model = course
    }
    const initialState = {
      code: courseId ? courseView.model.code : '',
      name: courseId ? courseView.model.name : '',
      subject: courseId ? (<Subject>courseView.model.subject)._id : '',
      teacher: courseId ? courseView.model.teacher || '' : ''
    }
    const form = new FormGroup({
      code: new FormControl(initialState.code, [Validators.required, Validators.pattern(CODE_REGEX)]),
      name: new FormControl(initialState.name, [Validators.required, Validators.pattern(TITLE_REGEX)]),
      subject: new FormControl(initialState.subject, [Validators.required, Validators.pattern(/^[a-f\d]{24}$/i)]),
      teacher: new FormControl(initialState.teacher, Validators.pattern(/^[a-f\d]{24}$/i))
    })
    courseView.form = form
    courseView.submit = async () => {
      const course: Course = {
        _id: courseId || new ObjectId().toHexString(),
        account: this.databaseService.accountId,
        term: termId,
        subject: form.get('subject').value,
        code: form.get('code').value,
        name: form.get('name').value
      }
      const teacher = form.get('teacher').value
      if (teacher) course.teacher = teacher
      this.popupService.performWithPopup(
        'Saving course',
        () => this.database.put.course(course),
        ErrorCodes.ERR_COURSE_EXISTS)
        .then(() => this.navigateToView(ViewType.TERM, termId))
    }
    const calendarId = `${(await this.database.fetch.term(termId)).calendar}`
    courseView.subjects.push(...await this.database.all.subjects(this.databaseService.accountId))
    courseView.teachers.push(...await this.database.all.teachers(calendarId))
    if (courseId) {
      courseView.list.push(...await this.database.all.classes(courseId))
      courseView.tests.push(...await this.database.all.tests(courseId))
      courseView.deliverables.push(...await this.database.all.deliverables(courseId))
    }
    return courseView
  }

  private async setClassViewProperties(view: View, classId?: Class['_id'], courseId?: Course['_id']) {
    const classView: ClassView = { ...view, model: null, teachers: [] }
    if (classId) {
      const klass = await this.database.fetch.class(classId)
      courseId = `${klass.course}`
      classView.parentId = courseId
      klass.course = await this.database.fetch.course(`${klass.course}`)
      klass.course.subject = await this.database.fetch.subject(`${klass.course.subject}`)
      classView.model = klass
    }
    const course = classId ? (<Course>classView.model.course) : await this.database.fetch.course(courseId)
    const initialState = {
      code: classId ? classView.model.code : '',
      start: classId ? classView.model.start : '08:00',
      end: classId ? classView.model.end : '09:30',
      repeat: Object.keys(ScheduleRepeatDay).reduce((obj, key) => {
        obj[key] = classId ? (<RelativeWeeklyScheduleRepeat>classView.model.repeat).days.includes(ScheduleRepeatDay[key]) : false
        return obj
      }, {}),
      teacher: classId ? classView.model.teacher || '' : course.teacher,
      location: classId ? JSON.stringify(classView.model.location) : ''
    }
    const form = new FormGroup({
      code: new FormControl(initialState.code, [Validators.required, Validators.pattern(CODE_REGEX)]),
      start: new FormControl(initialState.start, [Validators.required, this.getDateValidator()]),
      end: new FormControl(initialState.end, [Validators.required, this.getDateValidator()]),
      // schedule: new FormControl(initialState.schedule, [Validators.required, this.getJSONValidator<Schedule>('badSchedule')]),
      repeat: new FormGroup(Object.keys(initialState.repeat).reduce((obj, key) => {
        obj[key] = new FormControl(initialState.repeat[key])
        return obj
      }, {})),
      teacher: new FormControl(initialState.teacher, Validators.pattern(/^[a-f\d]{24}$/i)),
      location: new FormControl(initialState.location, this.getJSONValidator<Location>('badLocation'))
    })
    classView.form = form
    classView.submit = async () => {
      const klass: Class = {
        _id: classId || new ObjectId().toHexString(),
        account: this.databaseService.accountId,
        course: courseId,
        code: form.get('code').value,
        start: form.get('start').value,
        end: form.get('end').value,
        repeat: <RelativeWeeklyScheduleRepeat>{
          basis: ScheduleRepeatBasis.WEEK,
          days: Object.keys(ScheduleRepeatDay)
            .filter(key => (<FormGroup>form.get('repeat')).get(key).value)
            .map(key => ScheduleRepeatDay[key])
        }
      }
      const teacher = form.get('teacher').value
      if (teacher) klass.teacher = teacher
      this.popupService.performWithPopup(
        'Saving class',
        () => this.database.put.class(klass),
        ErrorCodes.ERR_CLASS_EXISTS)
        .then(() => this.navigateToView(ViewType.COURSE, courseId))
    }
    const calendarId = `${(await this.database.fetch.term(`${course.term}`)).calendar}`
    classView.teachers.push(...await this.database.all.teachers(calendarId))
    return classView
  }

  private async setDeliverableViewProperties(view: View, deliverableId?: Deliverable['_id'], courseId?: Course['_id']) {
    const deliverableView: DeliverableView = { ...view, model: null }
    if (deliverableId) {
      const deliverable = await this.database.fetch.deliverable(deliverableId)
      courseId = `${deliverable.course}`
      deliverableView.parentId = courseId
      deliverable.course = await this.database.fetch.course(courseId)
      deliverable.course.subject = await this.database.fetch.subject(`${deliverable.course.subject}`)
      deliverableView.model = deliverable
    }
    const initialState = {
      title: deliverableId ? deliverableView.model.title : '',
      deadline: deliverableId ? this.util.toHTMLDatetime(deliverableView.model.deadline) : '',
      description: deliverableId ? deliverableView.model.description : '',
      percentComplete: deliverableId ? deliverableView.model.percentComplete : 0
    }
    const course = deliverableId ? (<Course>deliverableView.model.course) : await this.database.fetch.course(courseId)
    const term = await this.database.fetch.term(`${course.term}`)
    const form = new FormGroup({
      title: new FormControl(initialState.title, [Validators.required, Validators.pattern(TITLE_REGEX)]),
      deadline: new FormControl(initialState.deadline, [Validators.required, this.getDateValidator(new Date(term.start), new Date(term.end))]),
      description: new FormControl(initialState.description),
      percentComplete: new FormControl(initialState.percentComplete, [Validators.required, Validators.min(0), Validators.max(100)])
    })
    deliverableView.form = form
    deliverableView.submit = async () => {
      const deliverable: Deliverable = {
        _id: deliverableId || new ObjectId().toHexString(),
        account: this.databaseService.accountId,
        course: courseId,
        title: form.get('title').value,
        description: form.get('description').value,
        deadline: new Date(form.get('deadline').value).getTime(),
        percentComplete: parseFloat(form.get('percentComplete').value)
      }
      this.popupService.performWithPopup(
        'Saving deliverable',
        () => this.database.put.deliverable(deliverable))
        .then(() => this.navigateToView(ViewType.COURSE, courseId))
    }
    return deliverableView
  }

  private async setTestViewProperties(view: View, testId?: Test['_id'], courseId?: Course['_id']) {
    const testView: TestView = { ...view, model: null }
    if (testId) {
      const test = await this.database.fetch.test(testId)
      courseId = `${test.course}`
      testView.parentId = courseId
      test.course = await this.database.fetch.course(courseId)
      test.course.subject = await this.database.fetch.subject(`${test.course.subject}`)
      testView.model = test
    }
    const initialState = {
      title: testId ? testView.model.title : '',
      date: testId ? this.util.toHTMLDatetime(testView.model.date) : '',
      description: testId ? testView.model.description : '',
      scorePercent: testId ? testView.model.scorePercent : '',
      location: testId ? JSON.stringify(testView.model.location) : ''
    }
    const course = testId ? (<Course>testView.model.course) : await this.database.fetch.course(courseId)
    const term = await this.database.fetch.term(`${course.term}`)
    const form = new FormGroup({
      title: new FormControl(initialState.title, [Validators.required, Validators.pattern(TITLE_REGEX)]),
      date: new FormControl(initialState.date, [Validators.required, this.getDateValidator(new Date(term.start), new Date(term.end))]),
      description: new FormControl(initialState.description),
      scorePercent: new FormControl(initialState.scorePercent, [Validators.min(0), Validators.max(100)]),
      location: new FormControl(initialState.location, this.getJSONValidator<Location>('badLocation'))
    })
    testView.form = form
    testView.submit = async () => {
      const test: Test = {
        _id: testId || new ObjectId().toHexString(),
        account: this.databaseService.accountId,
        course: courseId,
        title: form.get('title').value,
        date: new Date(form.get('date').value).getTime(),
        description: form.get('description').value
      }
      if (form.get('scorePercent').value) {
        test.scorePercent = parseFloat(form.get('scorePercent').value)
      }
      this.popupService.performWithPopup(
        'Saving test',
        () => this.database.put.test(test))
        .then(() => this.navigateToView(ViewType.COURSE, courseId))
    }
    return testView
  }

  async removeCalendar(calendar: Calendar, navigate = true) {
    if (navigate && !confirm('Are you sure you want to remove this calendar?')) return
    this.popupService.performWithPopup(
      'Removing calendar',
      () => this.database.remove.calendar(calendar._id))
      .then(() => {
        if (navigate) this.navigateToView(ViewType.CALENDARS)
      })
  }

  async removeSubject(subject: Subject, navigate = true) {
    if (navigate && !confirm('Are you sure you want to remove this subject?')) return
    this.popupService.performWithPopup(
      'Removing subject',
      () => this.database.remove.subject(subject._id))
      .then(() => {
        if (navigate) this.navigateToView(ViewType.CALENDARS)
      })
  }

  async removeTerm(term: Term, navigate = true) {
    if (navigate && !confirm('Are you sure you want to remove this term?')) return
    this.popupService.performWithPopup(
      'Removing term',
      () => this.database.remove.term(term._id))
      .then(() => {
        if (navigate) this.navigateToView(ViewType.CALENDAR, (<Calendar>term.calendar)._id)
      })
  }

  async removeTeacher(teacher: Teacher, navigate = true) {
    if (navigate && !confirm('Are you sure you want to remove this teacher?')) return
    this.popupService.performWithPopup(
      'Removing teacher',
      () => this.database.remove.teacher(teacher._id))
      .then(() => {
        if (navigate) this.navigateToView(ViewType.CALENDAR, `${teacher.calendar}`)
      })
  }

  async removeCourse(course: Course, navigate = true) {
    if (navigate && !confirm('Are you sure you want to remove this course?')) return
    this.popupService.performWithPopup(
      'Removing course',
      () => this.database.remove.course(course._id))
      .then(() => {
        if (navigate) this.navigateToView(ViewType.TERM, `${course.term}`)
      })
  }

  async removeClass(klass: Class, navigate = true) {
    if (navigate && !confirm('Are you sure you want to remove this class?')) return
    this.popupService.performWithPopup(
      'Removing class',
      () => this.database.remove.class(klass._id))
      .then(() => {
        if (navigate) this.navigateToView(ViewType.COURSE, (<Course>klass.course)._id)
      })
  }

  async removeDeliverable(deliverable: Deliverable, navigate = true) {
    if (navigate && !confirm('Are you sure you want to remove this deliverable?')) return
    this.popupService.performWithPopup(
      'Removing deliverable',
      () => this.database.remove.deliverable(deliverable._id))
      .then(() => {
        if (navigate) this.navigateToView(ViewType.COURSE, (<Course>deliverable.course)._id)
      })
  }

  async removeTest(test: Test, navigate = true) {
    if (navigate && !confirm('Are you sure you want to remove this test?')) return
    this.popupService.performWithPopup(
      'Removing test',
      () => this.database.remove.test(test._id))
      .then(() => {
        if (navigate) this.navigateToView(ViewType.COURSE, (<Course>test.course)._id)
      })
  }

  pickLocation() {
    alert('pick not implemented')
  }

}
