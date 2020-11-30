import { EvaluationComponent } from "./evaluationComponent";

export interface Test extends EvaluationComponent {
  title: string
  description: string
  date: number
  scorePrecent?: number
}
