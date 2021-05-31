import { Group } from './group'
import { Person } from './person'
import { Model, ModelIndices } from './_model'

export interface LinkPersonGroup extends Model {
  person: Model['_id'] | Person
  group: Model['_id'] | Group
}

export class LinkPersonGroupIndices extends ModelIndices {
  public static getIndices(): string[] {
    return super.getIndices().concat('person', 'group')
  }
}
