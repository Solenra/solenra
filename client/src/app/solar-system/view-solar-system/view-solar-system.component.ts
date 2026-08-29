import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {SolarSystemService} from '../../core/service/solar-system.service';
import {forkJoin, interval, Subscription} from 'rxjs';
import {Integration, SolarSystem} from '../../core/model/solar-system';
import {ApiPage} from '../../core/model/api-page';
import {MatExpansionModule} from '@angular/material/expansion';
import {UIChart} from 'primeng/chart';
import {DatePipe, DecimalPipe} from '@angular/common';
import {StatusLabelComponent} from '../../core/component/status-label/status-label.component';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatMenuModule} from '@angular/material/menu';
import {MatButtonModule} from '@angular/material/button';
import { EditFieldsDialogComponent } from '../../core/component/edit-fields-dialog/edit-fields-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { ConnectIntegrationDialogComponent } from '../connect-integration-dialog/connect-integration-dialog.component';
import { ConfirmationDialogComponent } from '../../core/component/confirmation-dialog/confirmation-dialog.component';
import {EnergyPlanService} from '../../core/service/energy-plan.service';

@Component({
  selector: 'app-view-solar-system',
  imports: [
    MatExpansionModule,
    RouterLink,
    UIChart,
    DatePipe,
    DecimalPipe,
    StatusLabelComponent,
    MatCardModule,
    MatIconModule,
    MatMenuModule,
    MatButtonModule
  ],
  templateUrl: './view-solar-system.component.html',
  styleUrl: './view-solar-system.component.css'
})
export class ViewSolarSystemComponent implements OnInit, OnDestroy {

  private routeSub: Subscription | undefined;
  protected solarSystemId: number | undefined;
  protected solarSystem: SolarSystem | undefined;
  private loadingSolarSystem = false;
  private loadingSolarSystemError = false;

  roiChartData: any;
  roiChartOptions: any;
  energyPlanRoiChartData: any;
  energyPlanRoiChartOptions: any;
  energyPlanRevenueChartData: any;
  energyPlanRevenueChartOptions: any;
  currencySymbol = '$';
  integrations: Integration[] = [];
  reloadSubscription: Subscription | undefined;
  integrationClientIds: { [key: string]: string | undefined } = {};
  @ViewChild('energyPlansFileInput') energyPlansFileInput!: ElementRef<HTMLInputElement>;

  constructor(
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private solarSystemService: SolarSystemService,
    private energyPlanService: EnergyPlanService
  ) {
  }

  ngOnInit(): void {
    this.routeSub = this.route.params.subscribe(params => {
      this.solarSystemId = params['id'];
      if (this.reloadSubscription) {
        this.reloadSubscription.unsubscribe();
        this.reloadSubscription = undefined;
      }
      this.loadSolarSystem(true);
    });
  }

  ngOnDestroy() {
    if (this.routeSub) {
      this.routeSub.unsubscribe();
    }
    if (this.reloadSubscription) {
      this.reloadSubscription.unsubscribe();
    }
  }

  loadSolarSystem(setLoadingFlag: boolean) {
    if (setLoadingFlag) {
      this.loadingSolarSystem = true;
    }
    this.solarSystemService.searchSolarSystems(this.solarSystemId).subscribe({
      next: (solarSystemPage: ApiPage<SolarSystem>) => {
        if (solarSystemPage && solarSystemPage.content) {
          if (!this.solarSystem || JSON.stringify(this.solarSystem) !== JSON.stringify(solarSystemPage.content[0])) {
            this.solarSystem = solarSystemPage.content[0];
          }
        }

        let unsubReload = false;
        let hasReloadSub = false;
        if (this.solarSystem?.solarSystemIntegrations) {
          for (const solarSystemIntegration of this.solarSystem.solarSystemIntegrations) {
            if (solarSystemIntegration.integration.credentials.find((cred: any) => cred.type === 'client-id')) {
              this.integrationClientIds[solarSystemIntegration.integration.code] = solarSystemIntegration.integration.credentials.find((cred: any) => cred.type === 'client-id')?.value;
            }
            if (solarSystemIntegration.status?.autoReload && (!this.reloadSubscription || this.reloadSubscription.closed)) {
              this.reloadSubscription = interval(10000)
                .subscribe((val) => {
                  this.loadSolarSystem(false);
                });
              hasReloadSub = true
            } else if (!solarSystemIntegration.status.autoReload && this.reloadSubscription && !this.reloadSubscription.closed) {
              unsubReload = true;
            }
          }
        }
        if (!hasReloadSub && unsubReload && this.reloadSubscription) {
          this.reloadSubscription.unsubscribe();
        }

        if (this.solarSystem?.roiToDate) {
          this.roiChartOptions = {
            aspectRatio: 2,
            circumference: 180,
            rotation: -90,
            animation: {
              duration: 0
            },
            plugins: {
              legend: {
                display: false
              }
            }
          };
          let roiData = [this.solarSystem.calculatedSavings, (this.solarSystem.outlayCost - this.solarSystem.calculatedSavings)];
          if (this.solarSystem.calculatedSavings > this.solarSystem.outlayCost) {
            roiData = [this.solarSystem.calculatedSavings];
          }
          this.roiChartData = {
            labels: [
              'Savings and revenue',
              'Remaining outlay cost',
            ],
            datasets: [{
              data: roiData,
              backgroundColor: [
                'rgb(0, 128, 0)',
                'rgb(192, 192, 192)',
              ]
            }]
          };
        }

        if (this.solarSystem?.energyPlans) {
          this.solarSystem.energyPlans.sort((a, b) => {
            return a.startDate.localeCompare(b.startDate);
          });
          let energyPlanLabels = [];
          let energyPlanRoiData = [];
          let energyPlanExportRevenueData = [];
          let energyPlanSelfConsumptionRevenueData = [];
          for (const energyPlan of this.solarSystem.energyPlans) {
            energyPlanLabels.push(energyPlan.energyPlan.name);
            energyPlanRoiData.push(energyPlan.roiAnnualised);
            energyPlanExportRevenueData.push(energyPlan.cumulativeExportRevenue);
            energyPlanSelfConsumptionRevenueData.push(energyPlan.cumulativeSelfConsumptionSavings);
          }
          this.energyPlanRoiChartOptions = {
            scales: {
              y: {
                beginAtZero: true
              }
            }
          };
          if (energyPlanRoiData.length > 0) {
            this.energyPlanRoiChartData = {
              labels: energyPlanLabels,
              datasets: [{
                label: 'Energy plan annual return %',
                data: energyPlanRoiData,
                stack: 'Stack 0'
              }]
            };
          }

          this.energyPlanRevenueChartOptions = {
          };

          if (energyPlanExportRevenueData.length > 0 || energyPlanSelfConsumptionRevenueData.length > 0) {
            this.energyPlanRevenueChartData = {
              labels: energyPlanLabels,
              datasets: [
                {
                  label: 'Export revenue',
                  data: energyPlanExportRevenueData,
                  backgroundColor: ['rgb(0, 128, 0)'],
                  stack: 'Stack 0'
                },
                {
                  label: 'Self consumption savings',
                  data: energyPlanSelfConsumptionRevenueData,
                  backgroundColor: ['rgb(0, 128, 128)'],
                  stack: 'Stack 0'
                }
              ]
            };
          }
        }

        this.loadingSolarSystem = false;
      },
      error: (err: any) => {
        this.loadingSolarSystem = false;
        this.loadingSolarSystemError = true;
      },
      complete: () => { }
    });
  }

  onClickConnectIntegration(): void {
    const dialogRef = this.dialog.open(ConnectIntegrationDialogComponent, {
      width: '500px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.solarSystemService.saveIntegration(this.solarSystemId!, result).subscribe({
          next: () => {
            this.loadSolarSystem(false);
          },
          error: (err: any) => {
            console.error('Error connecting integration:', err);
            // TODO: Show error message to user
          }
        });
      }
    });
  }

  onClickDeleteIntegration(solarSystemIntegration: any): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Confirm Delete',
        text: `Are you sure you want to delete the ${solarSystemIntegration.integration.name} integration?`,
        confirmLabel: 'Delete',
        cancelLabel: 'Cancel',
        confirmEventEmit: true
      }
    });

    dialogRef.componentInstance.onConfirm.subscribe(() => {
      this.solarSystemService.deleteIntegration(this.solarSystemId!, solarSystemIntegration.integration.code).subscribe({
        next: () => {
          dialogRef.close();
          this.loadSolarSystem(false);
        },
        error: (err: any) => {
          console.error('Error deleting integration:', err);
          dialogRef.close();
          // TODO: Show error message to user
        }
      });
    });
  }

  onClickRetryIntegration(solarSystemIntegration: any): void {
    this.solarSystemService.setIntegrationStatus(this.solarSystemId!, solarSystemIntegration.integration.code, 'pending').subscribe({
      next: () => {
        this.loadSolarSystem(false);
      },
      error: (err: any) => {
        console.error('Error retrying solar system integration:', err);
      }
    });
  }

  onClickAddEnergyPlan(): void {
    if (!this.solarSystemId) {
      return;
    }

    this.energyPlanService.getAll().subscribe({
      next: (data: any) => {
        const energyPlans = (Array.isArray(data) ? data : data?.content) || [];
        const dialogRef = this.dialog.open(EditFieldsDialogComponent, {
          width: '420px',
          data: {
            title: 'Add Energy Plan',
            formData: {
              energyPlanId: null,
              startDate: '',
              endDate: ''
            },
            formFields: [
              { controlName: 'energyPlanId', label: 'Energy Plan', type: 'select', required: true, options: energyPlans, optionKey: 'id', optionLabel: 'name' },
              { controlName: 'startDate', label: 'Start date', type: 'datetime', required: true },
              { controlName: 'endDate', label: 'End date', type: 'datetime', required: false }
            ],
            longFields: false,
            saveButtonLabel: 'Save',
            saveEventLabel: 'Saving...',
            saveEventEmit: true
          }
        });

        dialogRef.componentInstance.onSave.subscribe((formData: any) => {
          const request = {
            energyPlan: {id: formData.energyPlanId},
            startDate: this.toIsoDateTime(formData.startDate),
            endDate: formData.endDate ? this.toIsoDateTime(formData.endDate) : null
          };
          this.solarSystemService.saveEnergyPlan(this.solarSystemId!, request).subscribe({
            next: () => {
              dialogRef.close();
              this.loadSolarSystem(true);
            },
            error: (err: any) => {
              console.error('Error saving solar system energy plan:', err);
            }
          });
        });
      },
      error: (err: any) => {
        console.error('Error loading energy plans:', err);
      }
    });
  }

  private toIsoDateTime(value: string): string {
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(value)) {
      return `${value}:00Z`;
    }
    return value;
  }

  onClickEditEnergyPlan(energyPlan: SolarSystem['energyPlans'][number]): void {
    const dialogRef = this.dialog.open(EditFieldsDialogComponent, {
      width: '420px',
      data: {
        title: 'Edit Energy Plan Dates',
        formData: {
          startDate: this.toDateTimeInput(energyPlan.startDate),
          endDate: this.toDateTimeInput(energyPlan.endDate)
        },
        formFields: [
          { controlName: 'startDate', label: 'Start date', type: 'datetime', required: true },
          { controlName: 'endDate', label: 'End date', type: 'datetime', required: false }
        ],
        longFields: false,
        saveButtonLabel: 'Save',
        saveEventLabel: 'Saving...',
        saveEventEmit: true
      }
    });

    dialogRef.componentInstance.onSave.subscribe((formData: any) => {
      const request = {
        id: energyPlan.id,
        startDate: this.toIsoDateTime(formData.startDate),
        endDate: formData.endDate ? this.toIsoDateTime(formData.endDate) : null
      };
      this.solarSystemService.saveEnergyPlan(this.solarSystemId!, request).subscribe({
        next: () => {
          dialogRef.close();
          this.loadSolarSystem(true);
        },
        error: (err: any) => {
          console.error('Error updating solar system energy plan:', err);
        }
      });
    });
  }

  private toDateTimeInput(value?: string): string {
    return value ? value.substring(0, 16) : '';
  }

  onClickRemoveEnergyPlan(energyPlan: SolarSystem['energyPlans'][number]): void {
    if (!this.solarSystemId || !energyPlan.id) {
      return;
    }

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Confirm Delete',
        text: `Are you sure you want to remove the ${energyPlan.energyPlan.name} energy plan from this solar system?`,
        confirmLabel: 'Delete',
        cancelLabel: 'Cancel',
        confirmEventEmit: true
      }
    });

    dialogRef.componentInstance.onConfirm.subscribe(() => {
      this.solarSystemService.deleteEnergyPlan(this.solarSystemId!, energyPlan.id).subscribe({
        next: () => {
          dialogRef.close();
          this.loadSolarSystem(true);
        },
        error: (err: any) => {
          console.error('Error deleting solar system energy plan:', err);
          dialogRef.close();
        }
      });
    });
  }

  onClickExportEnergyPlans(): void {
    const csv = this.convertEnergyPlansToCsv(this.solarSystem?.energyPlans ?? []);
    const blob = new Blob([csv], {type: 'text/csv;charset=utf-8;'});
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'solar-system-energy-plans.csv';
    link.click();
    URL.revokeObjectURL(url);
  }

  convertEnergyPlansToCsv(energyPlans: SolarSystem['energyPlans']): string {
    const rows = [
      ['energyPlanId', 'startDate', 'endDate'],
      ...energyPlans.map(energyPlan => [
        energyPlan.energyPlan.id,
        energyPlan.startDate,
        energyPlan.endDate
      ])
    ];

    return rows
      .map(row => row.map(value => this.quoteCsvValue(value)).join(','))
      .join('\n');
  }

  private quoteCsvValue(value: unknown): string {
    return `"${String(value ?? '').replace(/"/g, '""')}"`;
  }

  onClickImportEnergyPlans(): void {
    this.energyPlansFileInput.nativeElement.value = '';
    this.energyPlansFileInput.nativeElement.click();
  }

  onImportEnergyPlansFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !this.solarSystemId) {
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      try {
        const rows = this.parseEnergyPlansCsv(String(reader.result || ''));
        const requests = rows.map(row => this.solarSystemService.saveEnergyPlan(this.solarSystemId!, {
          energyPlan: {id: row.energyPlanId},
          startDate: this.toIsoDateTime(row.startDate),
          endDate: row.endDate ? this.toIsoDateTime(row.endDate) : null
        }));

        if (requests.length === 0) {
          return;
        }

        forkJoin(requests).subscribe({
          next: () => this.loadSolarSystem(true),
          error: (err: any) => console.error('Error importing solar system energy plans:', err)
        });
      } catch (err) {
        console.error('Energy plans CSV import error:', err);
      }
    };
    reader.readAsText(file);
  }

  private parseEnergyPlansCsv(csv: string): Array<{energyPlanId: number; startDate: string; endDate?: string}> {
    const lines = csv.split(/\r?\n/).filter(line => line.trim().length > 0);
    if (lines.length < 2) {
      throw new Error('CSV must contain a header and at least one energy plan.');
    }

    const header = this.parseCsvLine(lines[0]).map(value => value.trim().toLowerCase());
    if (header.join(',') !== 'id,startdate,enddate') {
      throw new Error('CSV must contain the columns id, startDate, and endDate.');
    }

    return lines.slice(1).map((line, index) => {
      const values = this.parseCsvLine(line);
      const energyPlanId = Number(values[0]);
      if (!Number.isInteger(energyPlanId) || energyPlanId <= 0 || !values[1]) {
        throw new Error(`Invalid energy plan data on CSV row ${index + 2}.`);
      }
      if (Number.isNaN(new Date(values[1]).getTime()) || (values[2] && Number.isNaN(new Date(values[2]).getTime()))) {
        throw new Error(`Invalid date on CSV row ${index + 2}.`);
      }
      return {energyPlanId, startDate: values[1], endDate: values[2] || undefined};
    });
  }

  private parseCsvLine(line: string): string[] {
    const values: string[] = [];
    let value = '';
    let quoted = false;
    for (let index = 0; index < line.length; index++) {
      const character = line[index];
      if (character === '"') {
        if (quoted && line[index + 1] === '"') {
          value += '"';
          index++;
        } else {
          quoted = !quoted;
        }
      } else if (character === ',' && !quoted) {
        values.push(value);
        value = '';
      } else {
        value += character;
      }
    }
    values.push(value);
    return values;
  }

  onClickEdit() {
    this.openEditDialog(this.solarSystem);
  }
  
  openEditDialog(solarSystem?: SolarSystem): void {
    const isEditing = !!solarSystem;
    const dialogRef = this.dialog.open(EditFieldsDialogComponent, {
      width: '420px',
      data: {
        title: isEditing ? 'Edit Solar System' : 'Add Solar System',
        formData: {
          name: solarSystem?.name,
          outlayCost: solarSystem?.outlayCost,
          notes: solarSystem?.notes,
        },
        formFields: [
          { controlName: 'name', label: 'Name', type: 'text', required: true },
          { controlName: 'outlayCost', label: 'Outlay Cost', type: 'numeric', required: false },
          { controlName: 'notes', label: 'Notes', type: 'textarea', required: false }
        ],
        longFields: false,
        saveButtonLabel: 'Save',
        saveEventLabel: 'Saving...',
        saveEventEmit: true
      }
    });

    dialogRef.componentInstance.onSave.subscribe((formData: any) => {
      formData.id = solarSystem?.id;
      this.solarSystemService.save(formData).subscribe({
        next: () => {
          dialogRef.close();
          this.loadSolarSystem(true);
        },
        error: (err: any) => {
          console.error('Error saving solar system:', err);
        }
      });
    });
  }

}
