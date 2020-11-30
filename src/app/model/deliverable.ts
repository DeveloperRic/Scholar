import { EvaluationComponent, EvaluationComponentIndices } from "./evaluationComponent";
import { ModelIndices } from './_model';

export interface Deliverable extends EvaluationComponent {
  title: string
  description: string
  deadline: number
  percentComplete: number
}

export class DeliverableIndices extends EvaluationComponentIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('title', 'deadline', 'percentComplete')
  }
}
