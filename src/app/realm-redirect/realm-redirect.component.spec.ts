import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RealmRedirectComponent } from './realm-redirect.component';

describe('RealmRedirectComponent', () => {
  let component: RealmRedirectComponent;
  let fixture: ComponentFixture<RealmRedirectComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RealmRedirectComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RealmRedirectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
