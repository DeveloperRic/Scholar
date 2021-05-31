import { Injectable } from '@angular/core'
import { Account } from '../model/account'
import { DatabaseLink } from './databaseLink'
import { SyncService } from './sync.service'

@Injectable({
  providedIn: 'root'
})
export class DatabaseService {
  accountId: Account['_id']
  database: DatabaseLink

  constructor(private syncService: SyncService) {}

  async init() {
    console.log('DatabaseService: Initialising...')
    await this.syncService.init()
    this.accountId = this.syncService.getAccountId()
    this.database = this.syncService
    console.log('DatabaseService: Done initialising')
    return this.database
  }

  async logout() {
    await this.syncService.onlineDb.logout()
  }
}
