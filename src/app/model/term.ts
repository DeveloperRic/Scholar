import { Calendar } from "./calendar";
import { Model } from "./_model";

export interface Term extends Model {
  calendar: Model['_id'] | Calendar
}
