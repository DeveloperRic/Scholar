import { Subject } from "./subject";
import { Teacher } from "./teacher";
import { Model } from "./_model";

export interface Course extends Model {
  subject: Model['_id'] | Subject
  name: string
  code: string
  teacher: Model['_id'] | Teacher
}
