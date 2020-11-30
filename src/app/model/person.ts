import { Model, ModelIndices } from "./_model";

export interface Person extends Model {
  firstName: string
  lastName: string
  email: string
  /** milliseconds at 12:00 AM on DOB */
  dateOfBirth: number
}

export class PersonIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('firstName', 'lastName', '&email', 'dateOfBirth')
  }
}
