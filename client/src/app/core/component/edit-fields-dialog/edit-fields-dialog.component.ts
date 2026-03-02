import { AfterContentInit, Component, EventEmitter, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from "@angular/material/dialog";
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';

@Component({
  selector: 'app-edit-fields-dialog',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatCheckboxModule
  ],
  templateUrl: './edit-fields-dialog.component.html',
  styleUrls: ['./edit-fields-dialog.component.css']
})
export class EditFieldsDialogComponent implements OnInit, AfterContentInit {

  editFieldsForm!: FormGroup;
  fieldData: { [key: string]: any };

  isSaveButtonDisabled!: boolean;
  saveButtonText!: string;
  onSave = new EventEmitter();
  saveErrors!: string[];

  constructor(
    public dialogRef: MatDialogRef<EditFieldsDialogComponent>,
    @Inject( MAT_DIALOG_DATA ) public data: any,
    private fb: FormBuilder
  ) {
    this.fieldData = this.data.formData;
    this.createForm();
  }

  ngOnInit() {
    if (this.data.disableFields) {
      this.editFieldsForm.disable();
    }
    this.isSaveButtonDisabled = false;
    this.saveButtonText = this.data.saveButtonLabel;
  }

  ngAfterContentInit() {
    // track changes made by the user
    this.trackFormChanges();
  }

  createForm() {
    this.editFieldsForm = this.fb.group(this.data.formData);
  }

  trackFormChanges() {
    for (const formField of this.data.formFields) {
      console.log('Tracking form field:', formField.controlName); 
      if (formField.disabled) {
        const field = this.editFieldsForm.get(formField['controlName']);
        if (field) {
          field.disable();
        }
      } else {
        this.trackFormFieldChanges(formField['controlName']);
      }
    }
  }

  trackFormFieldChanges(fieldName: string) {
    const fieldControl = this.editFieldsForm.get(fieldName);

    if (fieldControl) {
      fieldControl.valueChanges.forEach(
        (updatedValue: any) => {
          // update the value field with the new data from the form input
          this.fieldData[fieldName] = updatedValue;
        }
      );
    }
  }

  /**
   * Notify that the save button has been clicked.
   */
  saveEvent() {
    if (this.data.saveEventEmit) {
      this.saveErrors = [];
      if (this.data.saveEventLabel) {
        this.saveButtonText = this.data.saveEventLabel;
      }
      this.isSaveButtonDisabled = true;
      if (this.data.saveEventFailureEmitter) {
        this.data.saveEventFailureEmitter.subscribe((errorArray: any) => {
          this.saveErrors = errorArray;
          this.isSaveButtonDisabled = false;
          this.saveButtonText = this.data.saveButtonLabel;
        });
      }
      this.onSave.emit(this.fieldData);
    }
  }

}
