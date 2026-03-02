import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditRatePeriodsComponent } from './edit-rate-periods.component';

describe('EditRatePeriodsComponent', () => {
  let component: EditRatePeriodsComponent;
  let fixture: ComponentFixture<EditRatePeriodsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditRatePeriodsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditRatePeriodsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
