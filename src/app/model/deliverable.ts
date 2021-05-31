import { EvaluationComponent, EvaluationComponentIndices } from './evaluationComponent'

export interface Deliverable extends EvaluationComponent {
  title: string
  deadline: number
  /** Deliverable interface discriminator */
  percentComplete: number
  description: string
}

export class DeliverableIndices extends EvaluationComponentIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('title', 'deadline', 'percentComplete')
  }
}
