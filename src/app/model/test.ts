import { EvaluationComponent, EvaluationComponentIndices } from "./evaluationComponent";
import { ExamLocation } from './location';

export interface Test extends EvaluationComponent {
  title: string
  date: number
  description: string
  location?: ExamLocation
  /** Test interface discriminator */
  scorePercent?: number
}

export class TestIndices extends EvaluationComponentIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('title', 'description', 'date')
  }
}
