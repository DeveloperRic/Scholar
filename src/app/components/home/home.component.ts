import { Component, OnInit, Output } from '@angular/core'
import { DatabaseService } from '../../database/database.service'
import { UtilService } from '../../services/util.service'
import { ErrorCodes } from '../../services/ErrorCodes'
import { ClassOccurrence } from '../../model/class'
import { Reminder } from '../../model/reminder'
import { Model } from '../../model/_model'
import { Router } from '@angular/router'
import { ClassesWithinRangeResult, DatabaseLink, DeliverablesDueWithinRangeResult, TestsWithinRangeResult } from '../../database/databaseLink'
import { Course } from '../../model/course'
import { Term } from '../../model/term'
import { ViewName } from '../../manage/manage.component'

type Class = ClassesWithinRangeResult[0]
type Deliverable = DeliverablesDueWithinRangeResult[0]
type Test = TestsWithinRangeResult[0]

interface Card {
  type: CardType
  data: object
}

interface ClassCard extends Card {
  type: CardType.CLASS
  data: Class
}

interface DateCard extends Card {
  type: CardType.DATE
  data: Date
}

interface DeliverableCard extends Card {
  type: CardType.DELIVERABLE
  data: Deliverable
}

interface NoDataCard extends Card {
  type: CardType.NO_DATA
}

interface TestCard extends Card {
  type: CardType.TEST
  data: Test
}

enum CardType {
  DATE = 'date',
  DELIVERABLE = 'deliverable',
  CLASS = 'class',
  REMINDER = 'reminder',
  TEST = 'test',
  NO_DATA = 'no_data'
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  private static readonly ONE_DAY = 24 * 60 * 60 * 1000
  private database: DatabaseLink
  private lastMaxDate: Date
  @Output() databaseStatus = 'loading'
  @Output() loading = false
  @Output() cards: Card[] = []

  constructor(private router: Router, private databaseService: DatabaseService, private util: UtilService) { }

  ngOnInit(): void {
    console.log('Home: Initialising...')
    this.init()
  }

  private async init() {
    try {
      const database = await this.databaseService.init()
      this.database = database
      this.databaseStatus = 'connected'
      console.log('Home: Done initialising')
    } catch (err) {
      if (err.message == ErrorCodes.ERR_NOT_LOGGED_IN) {
        console.log('Home: Error: Not logged in, displaying login page')
        this.databaseStatus = 'login'
      } else {
        console.error('Home: Error: Init failed', err)
        this.databaseStatus = 'failed'
      }
      return
    }
    this.loadMore()
  }

  onScroll() { }

  loadMore() {
    // if there's no max date, set one to yesterday so we start loading from today
    const lastMaxDate = this.lastMaxDate || new Date(Date.now() - HomeComponent.ONE_DAY)
    const nextMinDate = new Date(lastMaxDate.getTime())
    nextMinDate.setDate(nextMinDate.getDate() + 1)
    nextMinDate.setHours(0, 0, 0, 0)
    const nextMaxDate = new Date(nextMinDate)
    const daysTillEndOfWeek = 6 - nextMaxDate.getDay()
    nextMaxDate.setDate(nextMaxDate.getDate() + daysTillEndOfWeek + 1)
    nextMaxDate.setMilliseconds(nextMaxDate.getMilliseconds() - 1) // set to 11:59pm on maxDate
    this.updateCards(nextMinDate, nextMaxDate)
  }

  private async updateCards(minDate: Date, maxDate?: Date) {
    if (this.loading) return
    this.loading = true
    //TODO this is all really inefficient
    if (!maxDate) maxDate = new Date(minDate.getTime() + 7 * HomeComponent.ONE_DAY) // ATTENTION! minDate + 7 days <= maxDate otherwise test duplication fails
    const minMaxDateDiff = maxDate.getTime() - minDate.getTime()
    if (minMaxDateDiff < 0) throw new Error('maxDate cannot be < minDate')
    if (minMaxDateDiff > 7 * HomeComponent.ONE_DAY) throw new Error('minDate + 7 <= maxDate')
    const cards: Card[] = []
    const classes = await this.database.search.classesWithinRange(minDate, maxDate)
    const classOccurrences: ClassOccurrence[] = []
    classes.forEach(klass => {
      for (let i = 0; i < 7; ++i) {
        const date = new Date(minDate)
        date.setDate(date.getDate() + i)
        const time = this.util.parseTimeStr(klass.start)
        date.setHours(time[0], time[1])
        if (date > maxDate) break
        if (date.getTime() > (<Term>(<Course>klass.course).term).end) break
        if (klass.repeat.days.includes(date.getDay())) {
          classOccurrences.push(<ClassOccurrence>{
            class: klass,
            date
          })
        }
      }
    })
    const deliverables = await this.database.search.deliverablesDueWithinRange(minDate, maxDate)
    //TODO add reminders
    //TODO add holidays
    const tests = await this.database.search.testsWithinRange(minDate, maxDate)
    const sortedObjects = this.sortObjects(classOccurrences, deliverables, tests)
    let previousDate: Date = undefined
    sortedObjects.forEach(obj => {
      const objectDate = this.getObjectDate(obj, true)
      if (!previousDate || objectDate > previousDate) {
        cards.push(<DateCard>{
          type: CardType.DATE,
          data: objectDate
        })
        previousDate = objectDate
      }
      if (this.util.objectIsClassOccurrence(obj)) {
        cards.push(<ClassCard>{
          type: CardType.CLASS,
          data: (<ClassOccurrence>obj).class
        })
      } else if (this.util.objectIsDeliverable(obj)) {
        cards.push(<DeliverableCard>{
          type: CardType.DELIVERABLE,
          data: obj
        })
      } else if (this.util.objectIsTest(obj)) {
        cards.push(<TestCard>{
          type: CardType.TEST,
          data: obj
        })
      } else console.error(obj)
    })
    if (cards.length == 0) {
      cards.push(
        <DateCard>{
          type: CardType.DATE,
          data: minDate
        },
        <NoDataCard>{ type: CardType.NO_DATA }
      )
    }
    this.cards.push(...cards)
    this.lastMaxDate = maxDate
    this.loading = false
  }

  private sortObjects(classes: ClassOccurrence[], deliverables: Deliverable[], tests: Test[]): (ClassOccurrence | Deliverable | Test)[] {
    return [...classes, ...deliverables, ...tests].sort((a, b): number => {
      const aDate = this.getObjectDate(a)
      const bDate = this.getObjectDate(b)
      return aDate < bDate ? -1 : aDate > bDate ? 1 : 0
    })
  }

  private getObjectDate(obj: ClassOccurrence | Deliverable | Test, ignoreTime = false): Date {
    let date: Date
    if (this.util.objectIsClassOccurrence(obj)) date = new Date((<ClassOccurrence>obj).date)
    else if (this.util.objectIsDeliverable(obj)) date = new Date((<Deliverable>obj).deadline)
    else date = new Date((<Test>obj).date)
    if (ignoreTime) date.setHours(0, 0, 0, 0)
    return date
  }

  navigateToManageView(name: ViewName, docId?: Model['_id'], parentId?: Model['_id']) {
    let url = `/manage?view=${name}`
    if (docId) url += `&docId=${docId}`
    if (parentId) url += `&parentId=${parentId}`
    this.router.navigateByUrl(url)
  }

  toggleReminderDone(reminder: Reminder) {
    reminder.done = !reminder.done
    alert('not implemne')
  }
}
