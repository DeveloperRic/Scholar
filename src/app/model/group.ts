import { Model, ModelIndices } from "./_model";

export interface Group extends Model {
  name: string
  password: string
}

export class GroupIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('&name')
  }
}
