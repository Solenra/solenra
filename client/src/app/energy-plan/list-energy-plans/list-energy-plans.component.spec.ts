import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListEnergyPlansComponent } from './list-energy-plans.component';

describe('ListEnergyPlansComponent', () => {
  let component: ListEnergyPlansComponent;
  let fixture: ComponentFixture<ListEnergyPlansComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListEnergyPlansComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListEnergyPlansComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
