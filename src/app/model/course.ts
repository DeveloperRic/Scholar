import { Subject } from "./subject";
import { Teacher } from "./teacher";
import { Term } from './term';
import { Model, ModelIndices } from "./_model";

export interface Course extends Model {
  term: Model['_id'] | Term
  subject: Model['_id'] | Subject
  name: string
  code: string
  teacher?: Model['_id'] | Teacher
}

export class CourseIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('term', 'subject', 'name', '&code')
  }
}
