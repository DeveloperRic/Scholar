import { Component, EventEmitter, OnInit, Output } from '@angular/core'
import { ViewInfo } from '../../manage.component'

@Component({
  selector: 'manage-index',
  templateUrl: './index.component.html',
  styleUrls: ['../../manage.component.css']
})
export class IndexComponent implements OnInit {
  @Output() pushViewEvent = new EventEmitter<ViewInfo>()

  constructor(
  ) { }

  ngOnInit() {
  }

  pushView(view: ViewInfo) {
    this.pushViewEvent.emit(view)
  }

}
