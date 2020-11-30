import { Course } from "./course";
import { Location } from "./location";
import { Schedule } from "./schedule";
import { Teacher } from "./teacher";
import { Model } from "./_model";

export interface Class extends Model {
  course: Model['_id'] | Course
  code: string
  schedule: Schedule
  teacher: Model['_id'] | Teacher
  location: Location
}
