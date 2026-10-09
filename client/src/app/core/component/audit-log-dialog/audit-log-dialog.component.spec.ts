import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NEVER } from 'rxjs';
import { SolarSystemService } from '../../service/solar-system.service';

import { AuditLogDialogComponent } from './audit-log-dialog.component';

describe('AuditLogDialogComponent', () => {
  let component: AuditLogDialogComponent;
  let fixture: ComponentFixture<AuditLogDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AuditLogDialogComponent],
      providers: [
        { provide: MatDialogRef, useValue: { close: jasmine.createSpy('close') } },
        { provide: MAT_DIALOG_DATA, useValue: { solarSystem: { id: 1 } } },
        { provide: SolarSystemService, useValue: { getAuditLog: () => NEVER } }
      ]
    });
    fixture = TestBed.createComponent(AuditLogDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
