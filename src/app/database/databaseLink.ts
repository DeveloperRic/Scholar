import { Observable } from 'rxjs'

import { Account } from '../model/account'
import { Calendar } from '../model/calendar'
import { Subject } from '../model/subject'
import { Term } from '../model/term'
import { Teacher } from '../model/teacher'
import { Course } from '../model/course'
import { Class } from '../model/class'
import { Deliverable } from '../model/deliverable'
import { Test } from '../model/test'

export interface DatabaseLink {
  all: {
    calendars: (accountId: Account['_id']) => Observable<Calendar[]>
    subjects: (accountId: Account['_id']) => Observable<Subject[]>
    terms: (calendarId: Calendar['_id']) => Observable<Term[]>
    teachers: (calendarId: Calendar['_id']) => Observable<Teacher[]>
    courses: (termId: Term['_id']) => Observable<Course[]>
    classes: (courseId: Course['_id']) => Observable<Class[]>
    deliverables: (courseId: Course['_id']) => Observable<Deliverable[]>
    tests: (courseId: Course['_id']) => Observable<Test[]>
  }
  fetch: {
    calendar: (_id: Calendar['_id']) => Observable<Calendar>
    subject: (_id: Subject['_id']) => Promise<Subject>
    term: (_id: Term['_id']) => Promise<Term>
    teacher: (_id: Teacher['_id']) => Promise<Teacher>
    course: (_id: Course['_id']) => Promise<Course>
    class: (_id: Class['_id']) => Promise<Class>
    deliverable: (_id: Deliverable['_id']) => Promise<Deliverable>
    test: (_id: Test['_id']) => Promise<Test>
  }
  //TODO realm doesnt return these ids
  put: {
    calendar: (calendar: Calendar) => Observable<Calendar['_id']>
    subject: (subject: Subject) => Promise<Subject['_id']>
    term: (term: Term) => Promise<Term['_id']>
    teacher: (teacher: Teacher) => Promise<Teacher['_id']>
    course: (course: Course) => Promise<Course['_id']>
    class: (klass: Class) => Promise<Class['_id']>
    deliverable: (deliverable: Deliverable) => Promise<Deliverable['_id']>
    test: (test: Test) => Promise<Test['_id']>
  }
  remove: {
    calendar: (_id: Calendar['_id']) => Observable<void>
    subject: (_id: Subject['_id']) => Promise<void>
    term: (_id: Term['_id']) => Promise<void>
    teacher: (_id: Teacher['_id']) => Promise<void>
    course: (_id: Course['_id']) => Promise<void>
    class: (_id: Class['_id']) => Promise<void>
    deliverable: (_id: Deliverable['_id']) => Promise<void>
    test: (_id: Test['_id']) => Promise<void>
  }
  search: {
    classesWithinRange: (min: Date, max: Date) => Promise<Class[]>
    deliverablesDueWithinRange: (min: Date, max: Date) => Promise<Deliverable[]>
    testsWithinRange: (min: Date, max: Date) => Promise<Test[]>
  }
}
