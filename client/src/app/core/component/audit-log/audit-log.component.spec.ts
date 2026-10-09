import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NEVER } from 'rxjs';
import { SolarSystemService } from '../../service/solar-system.service';

import { AuditLogComponent } from './audit-log.component';

describe('AuditLogComponent', () => {
  let component: AuditLogComponent;
  let fixture: ComponentFixture<AuditLogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AuditLogComponent],
      providers: [
        { provide: SolarSystemService, useValue: { getAuditLog: () => NEVER } }
      ]
    });
    fixture = TestBed.createComponent(AuditLogComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('solarSystem', { id: 1 });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
