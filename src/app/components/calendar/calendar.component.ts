import { Component, OnInit, Output } from '@angular/core'

@Component({
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.css']
})
export class CalendarComponent implements OnInit {
  @Output() loading = true

  constructor() {}

  ngOnInit(): void {}

  onScroll() {}
}
