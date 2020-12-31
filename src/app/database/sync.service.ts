import { Injectable } from '@angular/core';
import { DatabaseLink } from "./databaseLink";
import { RealmService } from "./realm.service";
import { IndexedDBService } from "./indexedDB.service";
import { ErrorCodes } from "../services/ErrorCodes";
import { timer } from 'rxjs';
import { map } from 'rxjs/operators';
import { assert } from 'console';

@Injectable({
  providedIn: 'root'
})
export class SyncService implements DatabaseLink {
  private static readonly IS_ONLINE_DELAY = 10000
  public static isOnline = SyncService.setCheckOnlineTask()
  private isInitialised = false
  private isOnline = false
  //TODO enable sync between online & offline

  constructor(
    public onlineDb: RealmService,
    public offlineDb: IndexedDBService
  ) { }

  async init() {
    if (this.isInitialised) {
      console.log('SyncService: Already initialised')
      return
    }
    console.log('SyncService: Initialising...')
    console.log('SyncService: Initialising RealmService...')
    const loggedIn = this.onlineDb.isLoggedIn()
    if (!loggedIn) throw new Error(ErrorCodes.ERR_NOT_LOGGED_IN)
    console.log('SyncService: Initialising IndexedDB...')
    await this.offlineDb.init() // TODO error code for non local storage-persistance
    this.isOnline = window.navigator.onLine
    SyncService.isOnline.subscribe(isOnline => this.isOnline = isOnline)
    this.isInitialised = true
    console.log('SyncService: Done initialising')
  }

  getAccountId() {
    return this.onlineDb.getLoggedInUser().id
  }

  private static setCheckOnlineTask() {
    if (SyncService.isOnline !== undefined) return
    console.log('SyncService: enabling isOnline check')
    return timer(0, SyncService.IS_ONLINE_DELAY)
      .pipe(map(() => window.navigator.onLine))
  }

  all = this.onlineDb.all
  fetch = this.onlineDb.fetch
  put = this.onlineDb.put
  remove = this.onlineDb.remove
  search = this.onlineDb.search

}

