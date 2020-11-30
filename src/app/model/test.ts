import { EvaluationComponent, EvaluationComponentIndices } from "./evaluationComponent";

export interface Test extends EvaluationComponent {
  title: string
  description: string
  date: number
  scorePrecent?: number
}

export class TestIndices extends EvaluationComponentIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('title', 'description', 'date')
  }
}
