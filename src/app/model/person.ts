import { Model } from "./_model";

export interface Person extends Model {
  firstName: string
  lastName: string
  email: string
  /** milliseconds at 12:00 AM on DOB */
  dateOfBirth: number
}
