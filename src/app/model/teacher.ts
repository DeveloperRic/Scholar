import { Person } from './person'
import { ModelIndices } from './_model'

export interface Teacher extends Person {
  // In the future, Teacher will have more fields
}

export class TeacherIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('calendar')
  }
}
