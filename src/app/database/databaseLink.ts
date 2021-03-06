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

export type ClassesWithinRangeResult = (
  Omit<Class, 'course'> & { course: Course }
)[]
export type DeliverablesDueWithinRangeResult = (
  Omit<Deliverable, 'course'> & { course: Course }
)[]
export type TestsWithinRangeResult = (
  Omit<Test, 'course'> & { course: Course }
)[]

export interface DatabaseLink {
  all: {
    calendars: (accountId: Account['_id']) => Observable<Calendar[]>
    subjects: (accountId: Account['_id']) => Observable<Subject[]>
    terms: (calendarId: Calendar['_id']) => Observable<Term[]>
    teachers: (accountId: Account['_id']) => Observable<Teacher[]>
    courses: (termId: Term['_id']) => Observable<Course[]>
    classes: (courseId: Course['_id']) => Observable<Class[]>
    deliverables: (courseId: Course['_id']) => Observable<Deliverable[]>
    tests: (courseId: Course['_id']) => Observable<Test[]>
  }
  fetch: {
    calendar: (_id: Calendar['_id']) => Observable<Calendar>
    subject: (_id: Subject['_id']) => Observable<Subject>
    term: (_id: Term['_id']) => Observable<Term>
    teacher: (_id: Teacher['_id']) => Observable<Teacher>
    course: (_id: Course['_id']) => Observable<Course>
    class: (_id: Class['_id']) => Observable<Class>
    deliverable: (_id: Deliverable['_id']) => Observable<Deliverable>
    test: (_id: Test['_id']) => Observable<Test>
  }
  //TODO realm doesnt return these ids
  put: {
    calendar: (calendar: Calendar) => Observable<Calendar['_id']>
    subject: (subject: Subject) => Observable<Subject['_id']>
    term: (term: Term) => Observable<Term['_id']>
    teacher: (teacher: Teacher) => Observable<Teacher['_id']>
    course: (course: Course) => Observable<Course['_id']>
    class: (klass: Class) => Observable<Class['_id']>
    deliverable: (deliverable: Deliverable) => Observable<Deliverable['_id']>
    test: (test: Test) => Observable<Test['_id']>
  }
  remove: {
    calendar: (_id: Calendar['_id']) => Observable<void>
    subject: (_id: Subject['_id']) => Observable<void>
    term: (_id: Term['_id']) => Observable<void>
    teacher: (_id: Teacher['_id']) => Observable<void>
    course: (_id: Course['_id']) => Observable<void>
    class: (_id: Class['_id']) => Observable<void>
    deliverable: (_id: Deliverable['_id']) => Observable<void>
    test: (_id: Test['_id']) => Observable<void>
  }
  search: {
    classesWithinRange: (min: Date, max: Date) => Promise<ClassesWithinRangeResult>
    deliverablesDueWithinRange: (min: Date, max: Date) => Promise<DeliverablesDueWithinRangeResult>
    testsWithinRange: (min: Date, max: Date) => Promise<TestsWithinRangeResult>
  }
}
