import { Model, ModelIndices } from './_model'

export interface Calendar extends Model {
  year: number
  yearEnd: number
}

export class CalendarIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('year', 'group')
  }
}
