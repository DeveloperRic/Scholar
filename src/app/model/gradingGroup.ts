import { Course } from './course'
import { EvaluationComponent } from './evaluationComponent'
import { Model, ModelIndices } from './_model'

export enum GradingGroupMode {
  BY_AVERAGE = 'by_average',
  BY_TOP_N = 'by_top_n'
}

export interface GradingGroup extends Model {
  course: Course['_id'] | Course
  mode: GradingGroupMode
  weight: number
  evaluationComponents: EvaluationComponent['_id'][] | EvaluationComponent[]
}

export class GradingGroupIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('mode', 'weight')
  }
}
