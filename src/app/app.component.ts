import { Component, Output } from '@angular/core'
import { PopupService } from './services/popup.service'

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  @Output() title = 'scholar'

  constructor(public popupService: PopupService) {}
}
