import { Calendar } from './calendar'
import { Person } from './person'
import { Model, ModelIndices } from './_model'

export interface Teacher extends Person {
  calendar: Model['_id'] | Calendar
}

export class TeacherIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('calendar')
  }
}
