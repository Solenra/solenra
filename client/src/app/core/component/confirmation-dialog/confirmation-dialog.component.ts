import {Component, EventEmitter, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

import {MaterialModule} from '../../../material.module';
import {SafeHtmlPipe} from '../../pipe/safe-html.pipe';

@Component({
  selector: 'app-confirmation-dialog',
  templateUrl: './confirmation-dialog.component.html',
  imports: [
    MaterialModule,
    SafeHtmlPipe
],
  styleUrls: ['./confirmation-dialog.component.css']
})
export class ConfirmationDialogComponent implements OnInit {

  isConfirmButtonDisabled!: boolean;
  confirmButtonText!: string;
  onConfirm = new EventEmitter();
  isConfirm2ButtonDisabled!: boolean;
  confirm2ButtonText!: string;
  onConfirm2 = new EventEmitter();
  isConfirm3ButtonDisabled!: boolean;
  confirm3ButtonText!: string;
  onConfirm3 = new EventEmitter();

  constructor(
    public dialogRef: MatDialogRef<ConfirmationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit() {
    this.confirmButtonText = this.data.confirmLabel;
    this.confirm2ButtonText = this.data.confirm2Label;
    this.confirm3ButtonText = this.data.confirm3Label;
    this.isConfirmButtonDisabled = false;
  }

  /**
   * Notify that the confirm button has been clicked.
   */
  confirmEvent() {
    if (this.data.confirmEventEmit) {
      if (this.data.confirmEventLabel) {
        this.confirmButtonText = this.data.confirmEventLabel;
      }
      this.isConfirmButtonDisabled = true;
      this.onConfirm.emit();
    }
  }

  /**
   * Notify that the second confirm button has been clicked.
   */
  confirm2Event() {
    if (this.data.confirm2EventEmit) {
      if (this.data.confirm2EventLabel) {
        this.confirm2ButtonText = this.data.confirm2EventLabel;
      }
      this.isConfirm2ButtonDisabled = true;
      this.onConfirm2.emit();
    }
  }

  /**
   * Notify that the third confirm button has been clicked.
   */
  confirm3Event() {
    if (this.data.confirm3EventEmit) {
      if (this.data.confirm3EventLabel) {
        this.confirm3ButtonText = this.data.confirm3EventLabel;
      }
      this.isConfirm3ButtonDisabled = true;
      this.onConfirm3.emit();
    }
  }

}
