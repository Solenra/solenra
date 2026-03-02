import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {AuditLogComponent} from '../audit-log/audit-log.component';
import {MaterialModule} from '../../../material.module';

@Component({
  selector: 'app-audit-log-dialog',
  templateUrl: './audit-log-dialog.component.html',
  imports: [
    MaterialModule,
    AuditLogComponent
],
  styleUrls: ['./audit-log-dialog.component.css']
})
export class AuditLogDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<AuditLogDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

}
