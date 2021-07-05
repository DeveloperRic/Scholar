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

export const ID_REGEX = /^[a-f\d]{24}$/i
export const CODE_REGEX = /^\w[\w-]*$/
export const TITLE_REGEX = /^\w[\w -]*$/
export const NAME_REGEX = /^[^\s][^\n]*$/
export const EMAIL_SCHEMA_REGEX = new RegExp("^$|^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$")
