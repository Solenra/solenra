import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { EditFieldsDialogComponent } from '../../core/component/edit-fields-dialog/edit-fields-dialog.component';
import { PageHeaderComponent } from '../../core/component/page-header/page-header.component';
import { EnergyPlanService } from '../../core/service/energy-plan.service';
import { ConfirmationDialogComponent } from '../../core/component/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-edit-rate-periods',
  imports: [CommonModule, MatTableModule, MatButtonModule, MatIconModule, PageHeaderComponent],
  templateUrl: './edit-rate-periods.component.html',
  styleUrls: ['./edit-rate-periods.component.css'],
})
export class EditRatePeriodsComponent implements OnInit {

  energyPlanId?: number;
  energyPlan: any = null;

  displayedColumns: string[] = ['rateName', 'rateValue', 'comparativeRateValue', 'actions'];

  constructor(
    private energyPlanService: EnergyPlanService,
    private dialog: MatDialog,
    private route: ActivatedRoute,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.energyPlanId = +params['id'];
      if (this.energyPlanId) {
        this.load();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/energy-plans']);
  }

  load(): void {
    if (!this.energyPlanId) { return; }
    this.energyPlanService.get(this.energyPlanId).subscribe((data: any) => {
      this.energyPlan = data;
      if (!this.energyPlan.energyPlanRates) {
        this.energyPlan.energyPlanRates = [];
      }
    });
  }

  addRate(): void {
    this.openRateDialog();
  }

  editRate(rate: any): void {
    this.openRateDialog(rate);
  }

  deleteRate(rate: any): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Confirm Delete',
        text: 'Are you sure you want to delete this rate?',
        confirmLabel: 'Delete',
        cancelLabel: 'Cancel',
        confirmEventEmit: true
      }
    });

    dialogRef.componentInstance.onConfirm.subscribe(() => {
      const idx = this.energyPlan.energyPlanRates.findIndex((r: any) => r.id === rate.id);
      if (idx !== -1) {
        this.energyPlan.energyPlanRates.splice(idx, 1);
        this.savePlan(() => dialogRef.close());
      }
    });
  }

  editPeriods(rate: any): void {
    const dialogData = {
      title: `Edit Periods for ${rate.rateName}`,
      formData: { periods: rate.energyPlanRatePeriods || [] },
      formFields: [
        { controlName: 'periods', label: 'Periods', type: 'text', required: false }
      ],
      longFields: false,
      saveButtonLabel: 'Save',
      saveEventLabel: 'Saving...',
      saveEventEmit: true,
      saveEventFailureEmitter: null
    };

    this.openPeriodsManager(rate);
  }

  openPeriodsManager(rate: any): void {
    if (!rate.energyPlanRatePeriods) {
      rate.energyPlanRatePeriods = [];
    }

    // DayOfWeek options with title case display and uppercase values
    const dayOfWeekOptions = [
      { label: 'Sunday', value: 'SUNDAY' },
      { label: 'Monday', value: 'MONDAY' },
      { label: 'Tuesday', value: 'TUESDAY' },
      { label: 'Wednesday', value: 'WEDNESDAY' },
      { label: 'Thursday', value: 'THURSDAY' },
      { label: 'Friday', value: 'FRIDAY' },
      { label: 'Saturday', value: 'SATURDAY' }
    ];

    const dialogRef = this.dialog.open(EditFieldsDialogComponent, {
      width: '420px',
      data: {
        title: `Edit Rate Periods for ${rate.rateName}`,
        formData: { daysOfWeek: [], startTime: '', endTime: '' },
        formFields: [
          { controlName: 'daysOfWeek', label: 'Days of week', type: 'select', options: dayOfWeekOptions, multi: true, required: true },
          { controlName: 'startTime', label: 'Start time', type: 'time', required: true },
          { controlName: 'endTime', label: 'End time', type: 'time', required: true }
        ],
        longFields: false,
        saveButtonLabel: 'Add Period',
        saveEventLabel: 'Adding...',
        saveEventEmit: true
      }
    });

    dialogRef.componentInstance.onSave.subscribe((periodData: any) => {
      if (!rate.energyPlanRatePeriods) {
        rate.energyPlanRatePeriods = [];
      }
      // Convert daysOfWeek array format for API
      const newPeriod = {
        daysOfWeek: Array.isArray(periodData.daysOfWeek) 
          ? periodData.daysOfWeek.map((day: string) => ({ dayOfWeek: day }))
          : [{ dayOfWeek: periodData.daysOfWeek }],
        startTime: periodData.startTime,
        endTime: periodData.endTime
      };
      rate.energyPlanRatePeriods.push(newPeriod);
      this.savePlan(() => dialogRef.close());
    });
  }

  deletePeriod(rate: any, periodIdx: number): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Confirm Delete',
        text: 'Are you sure you want to delete this period?',
        confirmLabel: 'Delete',
        cancelLabel: 'Cancel',
        confirmEventEmit: true
      }
    });

    dialogRef.componentInstance.onConfirm.subscribe(() => {
      if (rate.energyPlanRatePeriods) {
        rate.energyPlanRatePeriods.splice(periodIdx, 1);
        this.savePlan(() => dialogRef.close());
      }
    });
  }

  openRateDialog(rate?: any): void {
    const isEditing = !!rate;
    const dialogData = {
      title: isEditing ? 'Edit Rate' : 'Add Rate',
      formData: rate ? { ...rate } : { rateName: '', rateValue: null, comparativeRateValue: null },
      formFields: [
        { controlName: 'rateName', label: 'Rate name', type: 'text', required: true },
        { controlName: 'rateValue', label: 'Rate value', type: 'numeric', required: false },
        { controlName: 'comparativeRateValue', label: 'Comparative rate', type: 'numeric', required: false }
      ],
      longFields: false,
      saveButtonLabel: 'Save',
      saveEventLabel: 'Saving...',
      saveEventEmit: true,
      saveEventFailureEmitter: null
    };

    const dialogRef = this.dialog.open(EditFieldsDialogComponent, { width: '420px', data: dialogData });

    dialogRef.componentInstance.onSave.subscribe((formData: any) => {
      if (isEditing) {
        // update existing
        const idx = this.energyPlan.energyPlanRates.findIndex((r: any) => r.id === rate.id);
        if (idx !== -1) {
          this.energyPlan.energyPlanRates[idx] = { ...this.energyPlan.energyPlanRates[idx], ...formData };
        }
      } else {
        // add new (no id assigned client-side)
        this.energyPlan.energyPlanRates.push(formData);
      }
      this.savePlan(() => dialogRef.close());
    });
  }

  savePlan(callback?: () => void): void {
    this.energyPlanService.save(this.energyPlan).subscribe({
      next: () => {
        this.load();
        if (callback) { callback(); }
      },
      error: (err: any) => {
        console.error('Error saving energy plan:', err);
      }
    });
  }

  getDayOfWeekName(dayValue: number): string {
    const days: { [key: number]: string } = {
      1: 'Monday',
      2: 'Tuesday',
      3: 'Wednesday',
      4: 'Thursday',
      5: 'Friday',
      6: 'Saturday',
      7: 'Sunday'
    };
    return days[dayValue] || 'Unknown';
  }

  getDaysOfWeekDisplay(period: any): string {
    if (!period.daysOfWeek || period.daysOfWeek.length === 0) {
      return 'No days';
    }
    const dayMap: { [key: string]: string } = {
      'MONDAY': 'Monday',
      'TUESDAY': 'Tuesday',
      'WEDNESDAY': 'Wednesday',
      'THURSDAY': 'Thursday',
      'FRIDAY': 'Friday',
      'SATURDAY': 'Saturday',
      'SUNDAY': 'Sunday'
    };
    return period.daysOfWeek.map((d: any) => dayMap[d.dayOfWeek] || d.dayOfWeek).join(', ');
  }

}
