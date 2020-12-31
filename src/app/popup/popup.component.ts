import { Component, OnInit, Output } from '@angular/core';
import { PopupService } from '../services/popup.service';

@Component({
  selector: 'app-popup',
  templateUrl: './popup.component.html',
  styleUrls: ['./popup.component.css']
})
export class PopupComponent implements OnInit {
  @Output() readonly popup: PopupService['popup']

  constructor(
    private popupService: PopupService
  ) {
    this.popup = popupService.popup
  }

  ngOnInit(): void {
  }

  dismiss() {
    this.popupService.dismissPopup()
  }
}
