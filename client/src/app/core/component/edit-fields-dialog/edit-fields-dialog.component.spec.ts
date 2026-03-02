import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditFieldsDialogComponent } from './edit-fields-dialog.component';

describe('EditFieldsDialogComponent', () => {
  let component: EditFieldsDialogComponent;
  let fixture: ComponentFixture<EditFieldsDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [EditFieldsDialogComponent]
    });
    fixture = TestBed.createComponent(EditFieldsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
