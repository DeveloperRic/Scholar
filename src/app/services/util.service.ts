import { Injectable } from '@angular/core'
import { ValidatorFn } from '@angular/forms'
import { Observable } from 'rxjs'
import { ClassOccurrence } from '../model/class'
import { Deliverable } from '../model/deliverable'
import { Hue } from '../model/hue'
import { Test } from '../model/test'
import { ErrorCodes } from './ErrorCodes'

@Injectable({
  providedIn: 'root'
})
export class UtilService {
  public hues: { name: string; value: Hue }[] = Object.keys(Hue)
    .sort()
    .map(name => ({ name, value: Hue[name] }))

  constructor() { }

  public parseHue(hue: Hue): { lighter: string; normal: string; darker: string } {
    const colours = hue.split('.')
    return { lighter: colours[0], normal: colours[1], darker: colours[2] }
  }

  /**
   * Parses an HTML date string
   * @param dateStr '<year>-<month 1-based>-<day 1-based>'
   * @param isFromHTMLDateField use to toggle if it's from a date field
   */
  public parseDateStr(dateStr: string, isFromHTMLDateField = false) {
    const parts = dateStr.split('-').map(part => parseInt(part))
    if (isFromHTMLDateField) parts[1]--
    return new Date(parts[0], parts[1], parts.length > 2 ? parts[2] : null)
  }

  /**
   * Converts a UTC Date object to an HTML `datetime-local` string
   * @param date date in UTC
   */
  public toHTMLDatetime(date: string | number | Date): string {
    const utcDate = new Date(date)
    const adjustedDate = new Date(utcDate.getTime() - new Date().getTimezoneOffset() * 60000)
    const dateStr = adjustedDate.toISOString()
    return dateStr.substr(0, dateStr.length - 1)
  }

  public padTime(num: number) {
    const str = `${num}`
    if (str.length >= 2) return str
    return `0${str}`
  }

  /**
   * Converts a Date object to an HTML `date` string: `2020-12-31`
   */
  public toHTMLDate(dateInUTC: string | number | Date): string {
    const utcDate = new Date(dateInUTC)
    // 2020-12-31 |  month is 0-based in JS and 1-based in HTML
    return `${utcDate.getFullYear()}-${this.padTime(utcDate.getMonth() + 1)}-${this.padTime(utcDate.getDate())}`
  }

  /**
   * Converts an HTML `date` string to a Date object
   */
  public fromHTMLDate(dateStrInLocalTime: string): Date {
    const utcDate = new Date(dateStrInLocalTime)
    // convert (2020-09-01T00:00:00 Z | 2020-08-31T20:00:00 -0400) to (2020-09-01T00:00:00 -0400 | 2020-09-01T04:00:00 Z)
    // Date constructor returns a UTC time. use the calculated timezone offset to set the correct time
    utcDate.setMinutes(utcDate.getMinutes() + utcDate.getTimezoneOffset())
    return utcDate
  }

  public convert24HrTo12Hr(time: string): string {
    const parts = time.split(':').map(part => parseInt(part))
    const isPM = parts[0] == 0 || parts[0] > 12
    if (parts[0] == 0) parts[0] = 24
    if (parts[0] > 12) parts[0] -= 12
    return `${parts.map(this.padTime).join(':')} ${isPM ? 'PM' : 'AM'}`
  }

  public parseTimeStr(time: string): number[] {
    return time.split(':').map(part => parseInt(part, 10))
  }

  public convertInputTimeStringToDate(time: string): Date {
    const [hour, min] = this.parseTimeStr(time)
    return new Date(0, 0, 0, hour, min)
  }

  public objectIsClassOccurrence(obj: ClassOccurrence | Deliverable | Test): obj is ClassOccurrence {
    return 'class' in obj && 'repeat' in obj.class
  }

  public objectIsDeliverable(obj: ClassOccurrence | Deliverable | Test): obj is Deliverable {
    return 'percentComplete' in obj
  }

  public objectIsTest(obj: ClassOccurrence | Deliverable | Test): obj is Test {
    return 'scorePercent' in obj || 'date' in obj
  }

  getErrorCode(error: any): ErrorCodes | null {
    if (error.error) {
      try {
        const realmErr = JSON.parse(error.error)
        console.log('realmErr.message: ', realmErr.message) //TODO remove this
        return Object.values(ErrorCodes).includes(realmErr.message) ? realmErr.message : null
      } catch (err) { }
    }
    return null
  }

  errorMatchesCode(code: ErrorCodes, error: any): boolean {
    return this.getErrorCode(error) === code
  }

  public promiseToObservable<T>(func: () => Promise<T>): Observable<T> {
    return new Observable(sub => {
      func()
        .then(res => {
          sub.next(res)
          sub.complete()
        })
        .catch(err => sub.error(err))
    })
  }

  public getDateValidator(min?: Date, max?: Date): ValidatorFn {
    return control => {
      if (control.value == '') return {}
      try {
        const date = new Date(control.value)
        if ((min && date < min) || (max && date > max)) {
          return { badDate: { value: control.value } }
        }
      } catch {
        return { badDate: { value: control.value } }
      }
      return {}
    }
  }

  public getHueValidator(): ValidatorFn {
    return control => {
      if (control.value == '') return {}
      if (!Object.values(Hue).includes(control.value)) return { badHue: { value: control.value } }
      return {}
    }
  }

  public getJSONValidator<T extends {}>(errorKey: string): ValidatorFn {
    return control => {
      const errors = {}
      if (control.value == '') return errors
      try {
        const _obj: T = JSON.parse(control.value)
      } catch {
        errors[errorKey] = { value: control.value }
      }
      return errors
    }
  }
}
