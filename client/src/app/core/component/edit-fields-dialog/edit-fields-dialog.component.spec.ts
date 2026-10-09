import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { EditFieldsDialogComponent } from './edit-fields-dialog.component';

describe('EditFieldsDialogComponent', () => {
  let component: EditFieldsDialogComponent;
  let fixture: ComponentFixture<EditFieldsDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [EditFieldsDialogComponent],
      providers: [
        { provide: MatDialogRef, useValue: { close: jasmine.createSpy('close') } },
        { provide: MAT_DIALOG_DATA, useValue: { formData: {}, formFields: [], saveButtonLabel: 'Save' } }
      ]
    });
    fixture = TestBed.createComponent(EditFieldsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
