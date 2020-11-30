import { Evaluation } from "./evaluation";
import { Model } from "./_model";

export interface EvaluationComponent extends Model {
  evaluation: Model['_id'] | Evaluation
}
