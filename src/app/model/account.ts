import { Person } from "./person";
import { Model } from "./_model";

export interface Account {
  _id: Model['_id']
  profile?: Model['_id'] | Person
}
