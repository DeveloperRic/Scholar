import { Location } from "./location";
import { Schedule } from "./schedule";
import { Model } from "./_model";

export interface Event extends Model {
  title: string
  description: string
  schedule: Schedule
  location: Location
}
