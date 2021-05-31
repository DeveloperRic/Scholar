import { Component, Input, OnInit } from '@angular/core'
import { PopupConfig, PopupService } from '../../../services/popup.service'

@Component({
  selector: 'app-popup',
  templateUrl: './popup.component.html',
  styleUrls: ['./popup.component.css']
})
export class PopupComponent implements OnInit {
  @Input() readonly popup: PopupConfig

  constructor(public popupService: PopupService) {}

  ngOnInit(): void {}

  dismiss() {
    this.popupService.dismissPopup()
  }
}
