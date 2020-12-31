import { Hue } from "./hue";
import { Model, ModelIndices } from "./_model";

export interface Subject extends Model {
  code: string
  name: string
  hue: Hue
}

export class SubjectIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('name', '&code')
  }
}
