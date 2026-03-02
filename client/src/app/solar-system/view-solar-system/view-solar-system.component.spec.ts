import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewSolarSystemComponent } from './view-solar-system.component';

describe('ViewSolarSystemComponent', () => {
  let component: ViewSolarSystemComponent;
  let fixture: ComponentFixture<ViewSolarSystemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewSolarSystemComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewSolarSystemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
