import { Course } from './course'
import { Location } from './location'
import { RelativeWeeklyScheduleRepeat } from './schedule'
import { Teacher } from './teacher'
import { Model, ModelIndices } from './_model'

export interface Class extends Model {
  course: Model['_id'] | Course
  code: string
  start: string
  end: string
  /** Class interface discriminator */
  repeat: RelativeWeeklyScheduleRepeat
  teacher?: Model['_id'] | Teacher
  location?: Location
}

/** Not for database use; objects will not serialise properly */
export interface ClassOccurrence {
  class: Class
  date: Date
}

export class ClassIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('course', 'code', 'start', 'end')
  }
}
