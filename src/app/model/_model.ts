import { ObjectId } from 'bson';
import { Account } from "./account";

export interface Model {
  _id: ObjectId
  account: Model['_id'] | Account
}

export class ModelIndices {
  public static getIndices(): string[] {
    return ['&_id', 'account']
  }
}
