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

const DATABASE_NAME = 'ScholarDatabase'
const DATABASE_VERSION = 0
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
  reminder: ReminderIndices.getIndices().join(','),
  subjects: SubjectIndices.getIndices().join(','),
  teachers: TeacherIndices.getIndices().join(','),
  terms: TermIndices.getIndices().join(','),
  tests: TestIndices.getIndices().join(',')
}]

export class Database extends Dexie {
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

  public constructor() {
    super(DATABASE_NAME)
    this.version(DATABASE_VERSION).stores(VERSIONS[DATABASE_VERSION])
  }
}
