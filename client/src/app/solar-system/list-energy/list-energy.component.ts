import {AfterViewInit, Component, EventEmitter, HostListener, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {MaterialModule} from '../../material.module';

import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {FormControlPipe} from '../../core/pipe/form-control.pipe';
import {StatusLabelComponent} from '../../core/component/status-label/status-label.component';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {SystemEnergyDetails} from '../../core/model/solar-system';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {catchError, interval, map, merge, of, startWith, Subscription, switchMap} from 'rxjs';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {SolarSystemService} from '../../core/service/solar-system.service';
import {AuditLogDialogComponent} from '../../core/component/audit-log-dialog/audit-log-dialog.component';

@Component({
  selector: 'app-list-energy',
  imports: [
    MaterialModule,
    ReactiveFormsModule,
    FormControlPipe,
    FormsModule,
    StatusLabelComponent,
    RouterLink
],
  templateUrl: './list-energy.component.html',
  styleUrl: './list-energy.component.css'
})
export class ListEnergyComponent implements OnInit, AfterViewInit, OnDestroy {

  solarSystemId!: number;

  displayedColumns: string[] = ['processStatus', 'timeUnit', 'unit', 'startDate', 'endDate', 'productionValue', 'importValue', 'exportValue', 'consumptionValue', 'selfConsumptionValue'];
  systemEnergyDetailsDatabase!: SystemEnergyDetailsDatabase | null;
  data: SystemEnergyDetails[] = [];

  resultsLength = 0;
  isLoadingDataTable = true;
  isError = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  initialPageIndex: any;
  initialPageSize: any;
  initialSortActive: any;
  initialSortDirection: any;

  filterChange: EventEmitter<string> = new EventEmitter();
  tableDataChange: EventEmitter<string> = new EventEmitter();
  searchForm!: FormGroup;
  selectAll = false;
  rowSelections: any = {};
  selectedSystemEnergyDetailss: { [key: string]: SystemEnergyDetails } = {};
  selectedSystemEnergyDetailssCount: number = 0;

  doingRefresh: boolean = false;
  reloadSubscription!: Subscription;

  access: any = {};
  selectedStatusChangeAccess: { [key: string]: boolean } = {};

  constructor(
    public dialog: MatDialog,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private formBuilder: FormBuilder,
    private solarSystemService: SolarSystemService
  ) {
    // TODO remember paging state
    /*if (!this.dataService.serviceData['listEnergyPaging']) {
      this.dataService.serviceData['listEnergyPaging'] = {};
    } else {
      this.initialPageIndex = this.dataService.serviceData['listEnergyPaging'].pageIndex;
      this.initialPageSize = this.dataService.serviceData['listEnergyPaging'].pageSize;
      this.initialSortActive = this.dataService.serviceData['listEnergyPaging'].sortActive;
      this.initialSortDirection = this.dataService.serviceData['listEnergyPaging'].sortDirection;
    }*/

    if (!this.initialSortActive) {
      this.initialSortActive = "startDate";
    }
    if (!this.initialSortDirection) {
      this.initialSortDirection = "desc";
    }
    if (!this.initialPageSize) {
      this.initialPageSize = 10;
    }

    this.createForm();
    //this.populateFormFromUrl(this.dataService.serviceData['listEnergyPaging'].searchFormValue);
    this.populateFormFromUrl(null);
  }

  ngOnInit(): void {

    if (this.route.snapshot.paramMap.get('id')) {
      this.solarSystemId = Number(this.route.snapshot.paramMap.get('id'));
    } else {
      // TODO display error
    }

    /*this.solarSystemService.getStatuses().subscribe(returnedStatuses => {
      this.statuses = returnedStatuses;
      this.tableDataChange.emit(); // reload table if it happened to load first
    });*/

    /*this.userService.getUserSystemPermissions().subscribe(returnedUserSystemPrivileges => {
      this.userSystemPrivileges = returnedUserSystemPrivileges;
      this.tableDataChange.emit(); // reload table if it happened to load first
    });*/
  }

  ngAfterViewInit() {
    this.systemEnergyDetailsDatabase = new SystemEnergyDetailsDatabase(this.solarSystemService);

    const displayDataChanges = [
      this.sort.sortChange,
      this.paginator.page,
      this.filterChange,
      this.tableDataChange
    ];

    // If the user changes the sort order, filter, etc, reset back to the first page and stop auto refresh.
    this.sort.sortChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.stopRefresh();
    });
    this.filterChange.subscribe(() => {
      this.paginator.pageIndex = 0;
      this.stopRefresh();
    });
    this.tableDataChange.subscribe(() => this.doingRefresh = true);

    merge(...displayDataChanges)
      .pipe(
        startWith({}),
        switchMap(() => {
          this.selectAll = false;
          if (!this.doingRefresh) {
            this.isLoadingDataTable = true;
          }
          return this.systemEnergyDetailsDatabase!.searchSolarSystemEnergyDetails(
            this.solarSystemId,
            this.sort.active,
            this.sort.direction,
            this.paginator.pageIndex,
            this.paginator.pageSize,
            this.searchForm.value
          );
        }),
        map(data => {
          this.isLoadingDataTable = false;
          this.isError = false;

          this.setPreservedPaging();

          if (data === null) {
            this.stopRefresh();
            return [];
          }

          let doRefresh = false;

          //if (data.content) {
          //  for (const solarSystem of data.content) {
          // determine access user has for each solar system
          //this.access[solarSystem.id] = this.solarSystemService.getAccess(solarSystem, this.statuses, this.statusUserWorkflows, this.userSystemPrivileges, this.externalRoles);

          //if (...status && ...status.autoReload) {
          //  doRefresh = true;
          //}
          //  }
          //}

          if (doRefresh) {
            if (!this.reloadSubscription || this.reloadSubscription.closed) {
              this.reloadSubscription = interval(10000)
                .subscribe((val) => {
                  // reload table
                  this.tableDataChange.emit();
                });
            }
          } else {
            this.stopRefresh();
          }

          this.resultsLength = data.page.totalElements;
          return data.content;
        }),
        catchError((err, caught) => {
          this.stopRefresh();
          this.isLoadingDataTable = false;
          this.isError = true;
          console.error(err);
          return of([]);
        })
      )
      .subscribe(data => (this.data = data));
  }

  @HostListener('unloaded')
  ngOnDestroy(): void {
    if (this.reloadSubscription) {
      this.reloadSubscription.unsubscribe();
    }
  }

  setPreservedPaging() {
    /*this.dataService.serviceData['listEnergyPaging'].pageIndex = this.paginator.pageIndex;
    this.dataService.serviceData['listEnergyPaging'].pageSize = this.paginator.pageSize;
    this.dataService.serviceData['listEnergyPaging'].pageSize = this.paginator.pageSize;
    this.dataService.serviceData['listEnergyPaging'].sortActive = this.sort.active;
    this.dataService.serviceData['listEnergyPaging'].sortDirection = this.sort.direction;*/
  }

  stopRefresh() {
    if (this.reloadSubscription && !this.reloadSubscription.closed) {
      this.reloadSubscription.unsubscribe();
    }
    this.doingRefresh = false;
  }

  createForm() {
    this.searchForm = this.formBuilder.group({
      // set fields to blank
      nameLike: '',
      name: '',
      enabled: '',
      statusCodes: []
    });
  }

  doFilter() {
    this.filterChange.emit();
    //this.dataService.serviceData['listEnergyPaging'].searchFormValue = this.searchForm.value;
  }

  resetForm() {
    this.searchForm.reset();
    this.searchForm.patchValue({
      status: 'A'
    });
    this.doFilter();
  }

  populateFormFromUrl(existingFormValue: any) {
    this.route.params.subscribe(params => {
      let nameLike = params['nameLike'];
      let name = params['name'];
      let enabled = params['enabled'];
      if (!enabled && !('enabled' in params)) {
        //enabled = true;
      }

      let statusCodes = [];
      if (params['statusCodes']) {
        // split comma separated string into an array
        statusCodes = params['statusCodes'].split(',').map(function(statusCodes: string) {
          return statusCodes;
        });
      }

      if (existingFormValue) {
        this.searchForm.patchValue(existingFormValue);
      } else {
        // set form parameters with values from url
        this.searchForm.patchValue({
          nameLike: nameLike,
          name: name,
          enabled: enabled,
          statusCodes: statusCodes
        });
        //this.dataService.serviceData['listEnergyPaging'].searchFormValue = this.searchForm.value;
      }
    });
  }

  selectAllChange() {
    for (let solarSystem of this.data) {
      if (this.selectAll) {
        if (!this.rowSelections[solarSystem.id]) {
          this.rowSelections[solarSystem.id] = true;
        }
      } else {
        if (this.rowSelections[solarSystem.id]) {
          this.rowSelections[solarSystem.id] = false;
        }
      }

      this.selectionChange(solarSystem);
    }
  }

  selectionChange(systemEnergyDetails: SystemEnergyDetails) {
    if (this.rowSelections[systemEnergyDetails.id]) {
      this.selectedSystemEnergyDetailss[systemEnergyDetails.id] = systemEnergyDetails;
    } else {
      delete this.selectedSystemEnergyDetailss[systemEnergyDetails.id];
    }

    this.selectedSystemEnergyDetailssCount = Object.keys(this.selectedSystemEnergyDetailss).length;
  }

  openAuditLog(systemEnergyDetails: SystemEnergyDetails) {
    let auditLogDialogRef: MatDialogRef<AuditLogDialogComponent>;
    auditLogDialogRef = this.dialog.open(AuditLogDialogComponent, {
      data: {
        systemEnergyDetails: systemEnergyDetails
      },
      disableClose: true
    });
  }

}

export class SystemEnergyDetailsDatabase {

  constructor(private solarSystemService: SolarSystemService) {}

  searchSolarSystemEnergyDetails(solarSystemId: number, sort: string, order: string, pageIndex: number, pageSize: number, formValues: any) {
    return this.solarSystemService.searchSolarSystemEnergyDetails(solarSystemId, sort, order, pageIndex, pageSize, formValues);
  }

}
