import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { EditFieldsDialogComponent } from '../../core/component/edit-fields-dialog/edit-fields-dialog.component';
import { PageHeaderComponent } from '../../core/component/page-header/page-header.component';
import { EnergyPlanService } from '../../core/service/energy-plan.service';

@Component({
  selector: 'app-list-energy-plans',
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatTableModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatCardModule,
    MatIconModule,
    MatPaginatorModule,
    MatTooltipModule,
    PageHeaderComponent
  ],
  templateUrl: './list-energy-plans.component.html',
  styleUrls: ['./list-energy-plans.component.css']
})
export class ListEnergyPlansComponent implements OnInit {

  energyPlans: any[] = [];
  displayedColumns: string[] = ['name', 'notes', 'supplyRateValue', 'exportRateValue', 'shared', 'actions'];
  dataSource = new MatTableDataSource<any>([]);
  
  resultsLength = 0;
  isLoadingDataTable = false;
  initialPageIndex = 0;
  initialPageSize = 10;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  constructor(
    private energyPlanService: EnergyPlanService,
    public dialog: MatDialog,
    private router: Router
  ) { }

  // Trigger file input click
  triggerImport(): void {
    if (this.fileInput && this.fileInput.nativeElement) {
      this.fileInput.nativeElement.value = '';
      this.fileInput.nativeElement.click();
    }
  }

  onImportFile(event: any): void {
    const file = event.target.files && event.target.files[0];
    if (!file) { return; }
    const reader = new FileReader();
    reader.onload = () => {
      const text = String(reader.result || '');
      try {
        const plans = this.parseCsvToPlans(text);
        // save plans (one per plan) and reload after all saved
        const saves: any[] = [];
        for (const p of plans) {
          saves.push(this.energyPlanService.save(p));
        }
        forkJoin([saves]).subscribe({
          next: () => this.load(),
          error: (e: any) => console.error('Import save error', e)
        });
      } catch (err) {
        console.error('CSV parse error', err);
      }
    };
    reader.readAsText(file);
  }

  exportCsv(): void {
    this.energyPlanService.getAll().subscribe((data: any) => {
      const plans = (Array.isArray(data) ? data : data?.content) || [];
      const csv = this.convertPlansToCsv(plans);
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'energy-plans.csv';
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  // Convert flat plans data to CSV (one row per period; rates without periods still output one row)
  convertPlansToCsv(plans: any[]): string {
    const header = [
      'planId','planName','planNotes','shared','supplyRateValue','exportRateValue',
      'rateId','rateName','rateValue','comparativeRateValue',
      'periodId','daysOfWeek','startTime','endTime'
    ];
    const rows: string[][] = [header];
    for (const p of plans) {
      const planBase = [p.id, p.name, p.notes, p.shared, p.supplyRateValue, p.exportRateValue];
      const rates = p.energyPlanRates || [];
      if (rates.length === 0) {
        rows.push(planBase.concat(['','','','','','']).map(v => this.quoteCsv(v)));
      } else {
        for (const r of rates) {
          const periods = r.energyPlanRatePeriods || [];
          if (periods.length === 0) {
            rows.push(planBase.concat([r.id, r.rateName, r.rateValue, r.comparativeRateValue, '', '', '', '']).map(v => this.quoteCsv(v)));
          } else {
            for (const per of periods) {
              const days = (per.daysOfWeek || []).map((d: any) => d.dayOfWeek).join(';');
              rows.push(planBase.concat([r.id, r.rateName, r.rateValue, r.comparativeRateValue, per.id || '', days, per.startTime, per.endTime]).map(v => this.quoteCsv(v)));
            }
          }
        }
      }
    }
    return rows.map(r => r.join(',')).join('\n');
  }

  quoteCsv(value: any): string {
    if (value === null || value === undefined) { return '""'; }
    const s = String(value).replace(/"/g, '""');
    return '"' + s + '"';
  }

  // Parse CSV produced by convertPlansToCsv back into plan objects
  parseCsvToPlans(csvText: string): any[] {
    const lines = csvText.split(/\r?\n/).filter(l => l.trim().length > 0);
    if (lines.length <= 1) { return []; }
    const rows = lines.map(line => this.simpleCsvParseLine(line));
    const dataRows = rows.slice(1);
    const plansMap = new Map<string, any>();
    for (const r of dataRows) {
      const [planId, planName, planNotes, shared, supplyRateValue, exportRateValue, rateId, rateName, rateValue, comparativeRateValue, periodId, daysOfWeek, startTime, endTime] = r;
      const planKey = planId || planName;
      let plan = plansMap.get(planKey);
      if (!plan) {
        plan = { id: planId ? Number(planId) : undefined, name: planName, notes: planNotes, shared: shared === 'true' || shared === 'True', supplyRateValue: supplyRateValue ? Number(supplyRateValue) : null, exportRateValue: exportRateValue ? Number(exportRateValue) : null, energyPlanRates: [] };
        plansMap.set(planKey, plan);
      }
      if (!rateName) { continue; }
      let rate = plan.energyPlanRates.find((x: any) => String(x.rateName) === String(rateName) && (x.id || '') === (rateId || ''));
      if (!rate) {
        rate = { id: rateId ? Number(rateId) : undefined, rateName, rateValue: rateValue ? Number(rateValue) : null, comparativeRateValue: comparativeRateValue ? Number(comparativeRateValue) : null, energyPlanRatePeriods: [] };
        plan.energyPlanRates.push(rate);
      }
      if (startTime || endTime || daysOfWeek) {
        const days = (daysOfWeek || '').split(';').filter((d: string) => d).map((d: string) => ({ dayOfWeek: d }));
        const period: any = { id: periodId ? Number(periodId) : undefined, daysOfWeek: days, startTime: startTime || null, endTime: endTime || null };
        rate.energyPlanRatePeriods.push(period);
      }
    }
    return Array.from(plansMap.values());
  }

  simpleCsvParseLine(line: string): string[] {
    // rudimentary CSV parser handling quoted fields
    const result: string[] = [];
    let cur = '';
    let inQuotes = false;
    for (let i = 0; i < line.length; i++) {
      const ch = line[i];
      if (inQuotes) {
        if (ch === '"') {
          if (line[i+1] === '"') { cur += '"'; i++; } else { inQuotes = false; }
        } else { cur += ch; }
      } else {
        if (ch === '"') { inQuotes = true; }
        else if (ch === ',') { result.push(cur); cur = ''; }
        else { cur += ch; }
      }
    }
    result.push(cur);
    return result;
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoadingDataTable = true;
    this.energyPlanService.getAll().subscribe({
      next: (data: any) => {
        console.log('Energy plans loaded:', data);
        // Handle paginated response or direct array
        const plans = (Array.isArray(data) ? data : data?.content) || [];
        this.energyPlans = plans;
        this.dataSource.data = plans;
        // wire paginator and sort
        setTimeout(() => {
          this.dataSource.paginator = this.paginator;
          if (this.sort) { this.dataSource.sort = this.sort; }
        });
        this.resultsLength = plans.length;
        this.isLoadingDataTable = false;
      },
      error: (err: any) => {
        console.error('Error loading energy plans:', err);
        this.isLoadingDataTable = false;
      }
    });
  }

  openDialog(plan?: any): void {
    const isEditing = !!plan;
    const dialogData = {
      title: isEditing ? 'Edit Energy Plan' : 'Create Energy Plan',
      formData: plan ? { ...plan } : { name: '', notes: '', shared: false, supplyRateValue: null, exportRateValue: null },
      formFields: [
        { controlName: 'name', label: 'Name', type: 'text', required: true },
        { controlName: 'notes', label: 'Notes', type: 'text', required: false },
        { controlName: 'supplyRateValue', label: 'Supply Rate Value', type: 'numeric', required: false },
        { controlName: 'exportRateValue', label: 'Export Rate Value', type: 'numeric', required: false },
        { controlName: 'shared', label: 'Shared', type: 'checkbox', required: false }
      ],
      longFields: false,
      saveButtonLabel: 'Save',
      saveEventLabel: 'Saving...',
      saveEventEmit: true,
      saveEventFailureEmitter: null
    };

    const dialogRef = this.dialog.open(EditFieldsDialogComponent, {
      width: '420px',
      data: dialogData
    });

    dialogRef.componentInstance.onSave.subscribe((formData: any) => {
      this.energyPlanService.save(formData).subscribe({
        next: () => {
          dialogRef.close();
          this.load();
        },
        error: (err: any) => {
          console.error('Error saving energy plan:', err);
        }
      });
    });
  }

  edit(plan: any): void {
    this.openDialog(plan);
  }

  openRateEditor(planId: number): void {
    this.router.navigate(['/energy-plans', planId, 'rates']);
  }

  delete(id: number): void {
    if (!confirm('Delete this energy plan?')) { return; }
    this.energyPlanService.delete(id).subscribe(() => this.load());
  }


}
