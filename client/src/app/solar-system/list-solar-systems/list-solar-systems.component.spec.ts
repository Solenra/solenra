import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListSolarSystemsComponent } from './list-solar-systems.component';

describe('ListSolarSystemsComponent', () => {
  let component: ListSolarSystemsComponent;
  let fixture: ComponentFixture<ListSolarSystemsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListSolarSystemsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListSolarSystemsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
