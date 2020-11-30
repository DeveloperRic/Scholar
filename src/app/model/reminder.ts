import { EvaluationComponent } from "./evaluationComponent";
import { Model, ModelIndices } from "./_model";

export interface Reminder extends Model {
  evaluationComponent: Model['_id'] | EvaluationComponent
  date: number
  done: boolean
}

export class ReminderIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('evaluationComponent', 'date', 'done')
  }
}
