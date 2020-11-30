import { EvaluationComponent } from "./evaluationComponent";
import { Model } from "./_model";

export interface Reminder extends Model {
  evaluationComponent: Model['_id'] | EvaluationComponent
  date: number
  done: boolean
}
