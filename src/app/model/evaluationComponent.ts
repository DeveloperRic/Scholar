import { Course } from './course'
import { Model, ModelIndices } from './_model'

export interface EvaluationComponent extends Model {
  course: Model['_id'] | Course
  weight?: number
}

export class EvaluationComponentIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('course')
  }
}
