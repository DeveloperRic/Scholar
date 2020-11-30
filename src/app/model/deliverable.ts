import { EvaluationComponent } from "./evaluationComponent";

export interface Deliverable extends EvaluationComponent {
  title: string
  description: string
  deadline: number
  percentComplete: number
}
