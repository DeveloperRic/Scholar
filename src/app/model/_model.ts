import { Account } from "./account";

export interface Model {
  _id: string
  account: Model['_id'] | Account
}
