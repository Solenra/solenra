import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListEnergyComponent } from './list-energy.component';

describe('ListEnergyComponent', () => {
  let component: ListEnergyComponent;
  let fixture: ComponentFixture<ListEnergyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListEnergyComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListEnergyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
