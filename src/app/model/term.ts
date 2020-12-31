import { Calendar } from "./calendar";
import { Model, ModelIndices } from "./_model";

export interface Term extends Model {
  calendar: Model['_id'] | Calendar
  name: string
  start: number
  end: number
}

export class TermIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('calendar', 'name', 'start', 'end')
  }
}
