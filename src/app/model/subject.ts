import { Hue } from "./hue";
import { Term } from "./term";
import { Model } from "./_model";

export interface Subject extends Model {
  term: Model['_id'] | Term
  name: string
  code: string
  hue: Hue
}
