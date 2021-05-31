import { Group } from './group'
import { Model, ModelIndices } from './_model'

export interface Calendar extends Model {
  year: number
  group?: Model['_id'] | Group
}

export class CalendarIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('year', 'group')
  }
}
