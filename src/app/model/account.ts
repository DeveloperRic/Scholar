import { Person } from "./person";
import { Model, ModelIndices } from "./_model";

export interface Account extends Omit<Model, 'account'> {
  profile?: Model['_id'] | Person
}

export class AccountIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('profile')
  }
}
