import { Group } from "./group";
import { Model } from "./_model";

export interface Calendar extends Model {
  year: number
  group?: Model['_id'] | Group
}
