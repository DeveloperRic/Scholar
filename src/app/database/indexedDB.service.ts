import { Injectable } from '@angular/core';
import Dexie from 'dexie';
import { Account, AccountIndices } from '../model/account';
import { Calendar, CalendarIndices } from '../model/calendar';
import { Class, ClassIndices } from '../model/class';
import { Course, CourseIndices } from '../model/course';
import { Deliverable, DeliverableIndices } from '../model/deliverable';
import { Event, EventIndices } from '../model/event';
import { Group, GroupIndices } from '../model/group';
import { LinkPersonGroup, LinkPersonGroupIndices } from '../model/linkPersonGroup';
import { Person, PersonIndices } from '../model/person'
import { Reminder, ReminderIndices } from '../model/reminder'
import { Subject, SubjectIndices } from '../model/subject'
import { Teacher, TeacherIndices } from '../model/teacher'
import { Term, TermIndices } from '../model/term'
import { Test, TestIndices } from '../model/test'
import { UtilService } from '../services/util.service';
import { DatabaseLink } from './databaseLink';

const DATABASE_NAME = 'ScholarDatabase'
const DATABASE_VERSION = 1
const VERSIONS: Parameters<Dexie['Version']['prototype']['stores']>[0][] = [{
  accounts: AccountIndices.getIndices().join(','),
  calendars: CalendarIndices.getIndices().join(','),
  classes: ClassIndices.getIndices().join(','),
  courses: CourseIndices.getIndices().join(','),
  deliverables: DeliverableIndices.getIndices().join(','),
  events: EventIndices.getIndices().join(','),
  groups: GroupIndices.getIndices().join(','),
  linksPersonGroup: LinkPersonGroupIndices.getIndices().join(','),
  people: PersonIndices.getIndices().join(','),
  reminders: ReminderIndices.getIndices().join(','),
  subjects: SubjectIndices.getIndices().join(','),
  teachers: TeacherIndices.getIndices().join(','),
  terms: TermIndices.getIndices().join(','),
  tests: TestIndices.getIndices().join(',')
}]

@Injectable({
  providedIn: 'root'
})
export class IndexedDBService extends Dexie implements DatabaseLink {
  accounts: Dexie.Table<Account, Account['_id']>
  calendars: Dexie.Table<Calendar, Calendar['_id']>
  classes: Dexie.Table<Class, Class['_id']>
  courses: Dexie.Table<Course, Course['_id']>
  deliverables: Dexie.Table<Deliverable, Deliverable['_id']>
  events: Dexie.Table<Event, Event['_id']>
  groups: Dexie.Table<Group, Group['_id']>
  linksPersonGroup: Dexie.Table<LinkPersonGroup, LinkPersonGroup['_id']>
  people: Dexie.Table<Person, Person['_id']>
  reminders: Dexie.Table<Reminder, Reminder['_id']>
  subjects: Dexie.Table<Subject, Subject['_id']>
  teachers: Dexie.Table<Teacher, Teacher['_id']>
  terms: Dexie.Table<Term, Term['_id']>
  tests: Dexie.Table<Test, Test['_id']>

  public constructor(
    private util: UtilService
  ) {
    super(DATABASE_NAME)
  }

  async init() {
    //TODO persist indexeddb
    if (this.isOpen()) {
      console.log('IndexedDB: Already initialised')
      return
    }
    console.log('IndexedDB: Initialising')
    console.log(`IndexedDB: Updating version to ${DATABASE_VERSION}`)
    const version = VERSIONS[DATABASE_VERSION - 1]
    this.version(DATABASE_VERSION).stores(version)
    console.log('IndexedDB: Verifying update')
    try {
      await this.open()
      console.log('IndexedDB: Done initialising')
    } catch (err) {
      throw new Error('Failed to initialise IndexedDB')
    }
  }

  fetch = {
    calendar: async (_id: Calendar['_id']) => await this.calendars.get(_id),
    subject: async (_id: Subject['_id']) => await this.subjects.get(_id),
    term: async (_id: Term['_id']) => await this.terms.get(_id),
    teacher: async (_id: Teacher['_id']) => await this.teachers.get(_id),
    course: async (_id: Course['_id']) => await this.courses.get(_id),
    class: async (_id: Class['_id']) => await this.classes.get(_id),
    deliverable: async (_id: Deliverable['_id']) => await this.deliverables.get(_id),
    test: async (_id: Test['_id']) => await this.tests.get(_id)
  }
  put = {
    calendar: async (calendar: Calendar) => await this.calendars.put(calendar),
    subject: async (subject: Subject) => await this.subjects.put(subject),
    term: async (term: Term) => await this.terms.put(term),
    teacher: async (teacher: Teacher) => await this.teachers.put(teacher),
    //TODO check that course does not exist
    course: async (course: Course) => await this.courses.put(course),
    class: async (klass: Class) => await this.classes.put(klass),
    deliverable: async (deliverable: Deliverable) => await this.deliverables.put(deliverable),
    test: async (test: Test) => await this.tests.put(test)
  }
  all = {
    calendars: async (accountId: Account['_id']) => await this.calendars.where({ account: accountId }).toArray(),
    subjects: async (accountId: Account['_id']) => await this.subjects.where({ account: accountId }).toArray(),
    terms: async (calendarId: Calendar['_id']) => await this.terms.where({ calendar: calendarId }).toArray(),
    teachers: async (calendarId: Calendar['_id']) => await this.teachers.where({ calendar: calendarId }).toArray(),
    courses: async (termId: Term['_id']) => await this.courses.where({ term: termId }).toArray(),
    classes: async (courseId: Course['_id']) => await this.classes.where({ course: courseId }).toArray(),
    deliverables: async (courseId: Course['_id']) => await this.deliverables.where({ course: courseId }).toArray(),
    tests: async (courseId: Course['_id']) => await this.tests.where({ course: courseId }).toArray(),
  }
  remove = {
    //TODO these should remove recursively (i.e. calendar>term>course....)
    calendar: async (_id: Calendar['_id']) => await this.calendars.delete(_id),
    subject: async (_id: Subject['_id']) => await this.subjects.delete(_id),
    term: async (_id: Term['_id']) => await this.terms.delete(_id),
    teacher: async (_id: Teacher['_id']) => await this.teachers.delete(_id),
    course: async (_id: Course['_id']) => await this.courses.delete(_id),
    class: async (_id: Class['_id']) => await this.classes.delete(_id),
    deliverable: async (_id: Deliverable['_id']) => await this.deliverables.delete(_id),
    test: async (_id: Test['_id']) => await this.tests.delete(_id)
  }
  search = {
    classesWithinRange: async (min: Date, max: Date) => {
      //TODO use transactions
      const courses = await this.coursesWithinRange(min, max)
      return (await this.classes
        .where('course').anyOf(courses.map(courses => courses._id)).toArray())
        .map(klass => {
          klass.course = courses.find(course => course._id === klass.course)
          return klass
        })
    },
    deliverablesDueWithinRange: async (min: Date, max: Date) => {
      const courses = await this.coursesWithinRange(min, max)
      return (await this.deliverables
        .where('course').anyOf(courses.map(courses => courses._id))
        .and(deliverable => deliverable.deadline >= min.getTime() && deliverable.deadline <= max.getTime())
        .toArray())
        .map(deliverable => {
          deliverable.course = courses.find(course => course._id === deliverable.course)
          return deliverable
        })
    },
    testsWithinRange: async (min: Date, max: Date) => {
      const courses = await this.coursesWithinRange(min, max)
      return (await this.tests
        .where('course').anyOf(courses.map(courses => courses._id))
        .and(test => test.date >= min.getTime() && test.date <= max.getTime())
        .toArray())
        .map(test => {
          test.course = courses.find(course => course._id === test.course)
          return test
        })
    }
  }

  private async coursesWithinRange(min: Date, max: Date) {
    const terms = await this.terms
      .where('start').aboveOrEqual(min.getTime()).and(term => term.start <= max.getTime()) // MIN <= start <= end <= MAX || MIN <= start <= MAX <= end
      .or('end').aboveOrEqual(min.getTime()).and(term => term.end <= max.getTime()) // start <= MIN <= end <= MAX
      .or('start').belowOrEqual(min.getTime()).and(term => term.end >= max.getTime()).toArray() // start <= MIN <= MAX <= end
    const courses = await this.courses.where('term').anyOf(terms.map(term => term._id)).toArray()
    for (const course of courses) {
      course.subject = await this.fetch.subject(`${course.subject}`)
    }
    return courses
  }

  /** Check if storage is persisted already.
  @returns {Promise<boolean>} Promise resolved with true if current origin is
  using persistent storage, false if not, and undefined if the API is not
  present.
  */
  async isStoragePersisted() {
    return navigator.storage && navigator.storage.persisted ?
      await navigator.storage.persisted() :
      undefined;
  }

  /** Tries to convert to persisted storage.
    @returns {Promise<boolean>} Promise resolved with true if successfully
    persisted the storage, false if not, and undefined if the API is not present.
  */
  async persist() {
    return navigator.storage && navigator.storage.persist ?
      await navigator.storage.persist() :
      undefined;
  }

  /** Queries available disk quota.
    @see https://developer.mozilla.org/en-US/docs/Web/API/StorageEstimate
    @returns {Promise<{quota: number, usage: number}>} Promise resolved with
    {quota: number, usage: number} or undefined.
  */
  async showEstimatedQuota() {
    return navigator.storage && navigator.storage.estimate ?
      await navigator.storage.estimate() :
      undefined;
  }

  /** Tries to persist storage without ever prompting user.
    @returns {Promise<string>}
      "never" In case persisting is not ever possible. Caller don't bother
        asking user for permission.
      "prompt" In case persisting would be possible if prompting user first.
      "persisted" In case this call successfully silently persisted the storage,
        or if it was already persisted.
  */
  async tryPersistWithoutPromtingUser() {
    if (!navigator.storage || !navigator.storage.persisted) {
      return "never";
    }
    let persisted = await navigator.storage.persisted();
    if (persisted) {
      return "persisted";
    }
    if (!navigator.permissions || !navigator.permissions.query) {
      return "prompt"; // It MAY be successful to prompt. Don't know.
    }
    const permission = await navigator.permissions.query({
      name: "persistent-storage"
    });
    if (permission.state === "granted") {
      persisted = await navigator.storage.persist();
      if (persisted) {
        return "persisted";
      } else {
        throw new Error("Failed to persist");
      }
    }
    if (permission.state === "prompt") {
      return "prompt";
    }
    return "never";
  }
}