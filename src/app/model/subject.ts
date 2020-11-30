import { Hue } from "./hue";
import { Term } from "./term";
import { Model, ModelIndices } from "./_model";

export interface Subject extends Model {
  term: Model['_id'] | Term
  name: string
  code: string
  hue: Hue
}

export class SubjectIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('term', 'name', '&code')
  }
}
