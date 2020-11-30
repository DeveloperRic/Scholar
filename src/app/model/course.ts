import { Subject } from "./subject";
import { Teacher } from "./teacher";
import { Model, ModelIndices } from "./_model";

export interface Course extends Model {
  subject: Model['_id'] | Subject
  name: string
  code: string
  teacher: Model['_id'] | Teacher
}

export class CourseIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('subject', 'name', '&code', 'teacher')
  }
}
