import {Component, EventEmitter, Inject, OnInit} from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
} from '@angular/material/dialog';
import {CommonModule} from '@angular/common';
import {MaterialModule} from '../../../material.module';
import {DataService} from '../../service/data.service';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-server-error',
  imports: [
    CommonModule,
    MaterialModule,
    RouterLink
  ],
  templateUrl: './server-error.component.html',
  styleUrl: './server-error.component.css'
})
export class ServerErrorComponent implements OnInit {

  titleDescription: string | undefined;
  userMessage: string | undefined;
  debugMessage: string | undefined;
  traceMessage: string | undefined;
  onCloseEvent = new EventEmitter();

  constructor(public dialogRef: MatDialogRef<any>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              public dataService: DataService) {
  }

  ngOnInit() {
    console.log('Server error dialog opened with data:', this.data);
    this.titleDescription = 'An Error Occurred';
    this.userMessage = this.data.message;
    if (this.data.errorMessage) {
      this.userMessage = this.data.errorMessage;
    }
    if (this.data.error) {
      if (this.data.error.error?.userMessage) {
        this.userMessage = this.data.error.error.userMessage;
      } else if (this.data.error.error?.errorMessage) {
        this.userMessage = this.data.error.error.errorMessage;
      }
      this.debugMessage = this.data.error.error?.message;
      this.traceMessage = this.data.error.error?.trace;
      if (this.data.error.status === 400) {
        this.titleDescription = 'Invalid Request';
      } else if (this.data.error.status === 401) {
        this.titleDescription = 'Unauthenticated User';
      } else if (this.data.error.status === 403) {
        this.titleDescription = 'Unauthorised User';
      }
    }
  }

  onCloseButton() {
    this.dataService.data['server-error-dialog-open'] = false;
    this.dataService.data['server-error-dialog-status'] = undefined;
    this.dialogRef.close();
    if (this.data.closeEventEmit) {
      this.onCloseEvent.emit();
    }
  }

}
