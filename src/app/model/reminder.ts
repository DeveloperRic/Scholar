import { EvaluationComponent } from './evaluationComponent'
import { Schedule } from './schedule'
import { Model, ModelIndices } from './_model'

export interface Reminder extends Model {
  evaluationComponent: Model['_id'] | EvaluationComponent
  schedule: Schedule
  done: boolean
}

export class ReminderIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('evaluationComponent', 'done')
  }
}
