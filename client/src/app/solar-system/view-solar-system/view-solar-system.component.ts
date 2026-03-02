import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {SolarSystemService} from '../../core/service/solar-system.service';
import {interval, Subscription} from 'rxjs';
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

  constructor(
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private solarSystemService: SolarSystemService
  ) {
  }

  ngOnInit(): void {
    this.routeSub = this.route.params.subscribe(params => {
      this.solarSystemId = params['id'];
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
    this.loadingSolarSystem = true;
    this.solarSystemService.searchSolarSystems(this.solarSystemId).subscribe({
      next: (solarSystemPage: ApiPage<SolarSystem>) => {
        if (solarSystemPage && solarSystemPage.content) {
          this.solarSystem = solarSystemPage.content[0];
        }

        let unsubReload = false;
        let hasReloadSub = false;
        if (this.solarSystem?.solarSystemIntegrations) {
          for (const solarSystemIntegration of this.solarSystem.solarSystemIntegrations) {
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

  // TODO
  onClickAddEnergyPlan() {

  }

  onClickEditEnergyPlan() {

  }

  onClickRemoveEnergyPlan() {

  }

  onClickExportEnergyPlans() {

  }

  onClickImportEnergyPlans() {

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
