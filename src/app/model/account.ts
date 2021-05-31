import { Person, PersonIndices } from './person'

export interface Account extends Person {}

export class AccountIndices extends PersonIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat()
  }
}
