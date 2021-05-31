import { Account } from './account'

export interface Model {
  _id: string
  account: Model['_id'] | Account
}

export class ModelIndices {
  public static getIndices(): string[] {
    return ['&_id', 'account']
  }
}
