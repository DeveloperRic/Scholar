import { Calendar } from "./calendar";
import { Person } from "./person";
import { Model } from "./_model";

export interface Teacher extends Person {
  calendar: Model['_id'] | Calendar
}
