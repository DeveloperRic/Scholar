import { Injectable } from '@angular/core';
import { UtilService } from './util.service';
import { ErrorCodes } from './ErrorCodes';

interface PopupConfig {
  type: 'error' | 'loading' | undefined
  message?: string
}

interface ErrorPopupConfig extends PopupConfig {
  type: 'error'
  error?: any
}

interface Popup extends PopupConfig {
  visible: boolean
}

@Injectable({
  providedIn: 'root'
})
export class PopupService {
  private static readonly POPUP_UNDEFINED: PopupConfig = { type: undefined }
  public readonly popup: Popup

  constructor(
    private util: UtilService
  ) {
    this.popup = { ...PopupService.POPUP_UNDEFINED, visible: false }
  }

  newPopup(popupConfig: ErrorPopupConfig | PopupConfig, visible = true): Readonly<Popup> {
    if (this.popup.type == undefined && popupConfig.type != undefined) { // 2nd condition prevents infinite loop when trying to clear the popup
      this.dismissPopup()
    }

    Object.entries(popupConfig).forEach(([key, value]) => this.popup[key] = value)
    if (!this.popup.message) this.popup.message = 'Something went wrong'
    this.popup.visible = visible
    if ('error' in popupConfig && popupConfig.error) console.error(popupConfig.error)
    return { ...this.popup } // copy the popup so we don't read-only lock our original popup object
  }

  dismissPopup() {
    this.newPopup(PopupService.POPUP_UNDEFINED, false)
  }

  performWithPopup<T>(message: string, func: () => Promise<T>, knownErr?: ErrorCodes): Promise<T> {
    this.newPopup({
      type: 'loading',
      message: message
    })
    return func()
      .then(result => {
        this.dismissPopup()
        return [result, null]
      })
      .catch(err => {
        if (!knownErr || !this.util.errorMatchesCode(knownErr, err)) {
          console.error(err)
        }
        this.newPopup({
          type: 'error',
          message: this.util.getErrorCode(err)
        })
        return [null, err]
      })
      .then(([result, err]) => {
        if (err) throw err
        return result
      })
  }
}
