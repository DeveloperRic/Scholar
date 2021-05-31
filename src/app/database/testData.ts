import { ObjectId } from 'bson'
import { Account } from '../model/account'
import { Calendar } from '../model/calendar'
import { Class } from '../model/class'
import { Course } from '../model/course'
import { Deliverable } from '../model/deliverable'
import { Event } from '../model/event'
import { Group } from '../model/group'
import { Hue } from '../model/hue'
import { Person } from '../model/person'
import { Reminder } from '../model/reminder'
import { RelativeWeeklyScheduleRepeat, ScheduleRepeatBasis, ScheduleRepeatDay } from '../model/schedule'
import { Subject } from '../model/subject'
import { Teacher } from '../model/teacher'
import { Term } from '../model/term'
import { Test } from '../model/test'
import { Model } from '../model/_model'
import { IndexedDBService } from './indexedDB.service'

const objects: Model[] = []

export const ACCOUNT: Account = {
  _id: new ObjectId().toHexString(),
  dateOfBirth: new Date(2020, 6, 19).getTime(),
  email: 'email@test.com',
  firstName: 'John',
  lastName: 'Doe'
}

export const PERSON: Person = {
  _id: new ObjectId().toHexString(),
  account: ACCOUNT._id,
  dateOfBirth: new Date(2020, 6, 19).getTime(),
  email: 'email@test.com',
  firstName: 'John',
  lastName: 'Doe'
}
objects.push(PERSON)

export const CALENDAR: Calendar = {
  _id: new ObjectId().toHexString(),
  account: ACCOUNT._id,
  year: 2020
}
objects.push(CALENDAR)

export const TERM: Term = {
  _id: new ObjectId().toHexString(),
  account: ACCOUNT._id,
  calendar: CALENDAR._id,
  name: 'Fall',
  start: new Date(2020, 8, 5).getTime(),
  end: new Date(2020, 11, 23).getTime()
}
objects.push(TERM)

export const SUBJECT: Subject = {
  _id: new ObjectId().toHexString(),
  account: ACCOUNT._id,
  code: 'COMP',
  name: 'Computer Science',
  hue: Hue.PURPLE
}
objects.push(SUBJECT)

export const TEACHER: Teacher = {
  _id: new ObjectId().toHexString(),
  account: ACCOUNT._id,
  calendar: CALENDAR._id,
  dateOfBirth: new Date(1980, 8, 1).getTime(),
  email: 'teacher@test.com',
  firstName: 'Comp',
  lastName: 'Teacher'
}
objects.push(TEACHER)

export const COURSE: Course = {
  _id: new ObjectId().toHexString(),
  account: ACCOUNT._id,
  term: TERM._id,
  subject: SUBJECT._id,
  code: '2402',
  name: 'Data Structures',
  teacher: TEACHER._id
}
objects.push(COURSE)

export const CLASS: Class = {
  _id: new ObjectId().toHexString(),
  account: ACCOUNT._id,
  course: COURSE._id,
  code: 'B',
  start: '08:00',
  end: '09:30',
  repeat: <RelativeWeeklyScheduleRepeat>{
    basis: ScheduleRepeatBasis.WEEK,
    days: [ScheduleRepeatDay.MONDAY, ScheduleRepeatDay.WEDNESDAY]
  },
  teacher: COURSE.teacher,
  location: {
    name: 'Herzberg Labs',
    url: 'google.com/q=herzberg+labs',
    coordinates: [45.38207758289972, -75.69763381693399]
  }
}
objects.push(CLASS)

export const DELIVERABLE: Deliverable = {
  _id: new ObjectId().toHexString(),
  account: ACCOUNT._id,
  course: COURSE._id,
  title: 'Design a Red-Black Tree',
  description: 'Make a red balck tree using the information we presented in class. Make sure it is fast and has no redundancies.',
  deadline: new Date(2021, 0, 1).getTime(),
  percentComplete: 35
}
objects.push(DELIVERABLE)

export const TEST: Test = {
  _id: new ObjectId().toHexString(),
  account: ACCOUNT._id,
  course: COURSE._id,
  title: 'Midterm',
  description: 'This test will mainly be on red-black trees',
  date: new Date(2021, 1, 15).getTime(),
  location: {
    name: 'Alumni Hall',
    seat: '94'
  },
  scorePercent: 79
}
objects.push(TEST)

export const REMINDER: Reminder = {
  _id: new ObjectId().toHexString(),
  account: ACCOUNT._id,
  evaluationComponent: TEST._id,
  schedule: { start: new Date(2020, 11, 31).getTime() },
  done: false
}
objects.push(REMINDER)

export const EVENT: Event = {
  _id: new ObjectId().toHexString(),
  account: ACCOUNT._id,
  title: 'Prof Night',
  description: 'come drink with your profs',
  schedule: {
    start: new Date(2020, 11, 20).getTime()
  },
  location: {
    name: "Mike's place"
  }
}
objects.push(EVENT)

export const GROUP: Group = {
  _id: new ObjectId().toHexString(),
  account: ACCOUNT._id,
  name: 'A+ club',
  password: 'arey0uanerd?'
}
objects.push(GROUP)

export function createTestData(db: IndexedDBService) {
  const objs: (Account | Model)[] = [...objects, ACCOUNT]
  console.log(objs)
  db.transaction('rw', 'accounts', trans => {
    console.log(ACCOUNT)
    db.accounts.put(ACCOUNT)
  })
  return objs
}

export function getModels() {
  return objects
}
