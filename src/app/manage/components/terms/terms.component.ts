import { Component, OnInit } from '@angular/core'
import { Router } from '@angular/router'
import { Observable } from 'rxjs'
import { map } from 'rxjs/operators'
import { DatabaseService } from 'src/app/database/database.service'
import { Term } from 'src/app/model/term'
import { PopupService } from 'src/app/services/popup.service'
import { FilterField } from '../filter-menu/filter-menu.component'

@Component({
  selector: 'manage-terms',
  templateUrl: './terms.component.html'
})
export class TermsComponent implements OnInit {
  terms$: Observable<Term[]>
  noTerms: boolean
  filterMenuVisible: boolean
  enabledFilters = [FilterField.CALENDAR]

  constructor(private router: Router, private databaseService: DatabaseService, private popupService: PopupService) {}

  ngOnInit() {
    this.terms$ = this.popupService.runWithPopup(
      'Fetching terms',
      this.databaseService.database.all.terms(this.databaseService.accountId).pipe(
        map(terms => {
          this.noTerms = terms.length === 0
          return terms
        })
      )
    )
  }

  onAddTermClick() {
    // TODO navigateToView(this.router, ViewType.TERM)
  }

  toggleFilterMenu() {
    this.filterMenuVisible = !this.filterMenuVisible
  }
}
