import { Course } from "./course";
import { Model } from "./_model";

export interface Evaluation extends Model {
  course: Model['_id'] | Course
}
