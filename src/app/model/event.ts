import { Location } from './location'
import { Schedule } from './schedule'
import { Model, ModelIndices } from './_model'

export interface Event extends Model {
  title: string
  description: string
  schedule: Schedule
  location: Location
}

export class EventIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('title')
  }
}
