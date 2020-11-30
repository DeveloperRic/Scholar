import { Group } from "./group";
import { Person } from "./person";
import { Model } from "./_model";

export interface LinkPersonGroup extends Model {
  person: Model['_id'] | Person
  group: Model['_id'] | Group
}
