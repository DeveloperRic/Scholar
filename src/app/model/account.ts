import { Person } from './person'
import { ModelIndices } from './_model'

export type GradingSettings = {
  gpaScale: {
    code: string // e.g. A+
    minPercent: number
    maxPercent: number
  }[]
  gpaHistory: {
    numCoursesNotInScholar: number
    cumulativeGpaBeforeScholar: number
  }
}

export interface Account extends Partial<Person> {
  _id: Person['_id']
  account: Person['account']
  settings?: {
    grading?: GradingSettings
  }
}

export class AccountIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat()
  }
}
