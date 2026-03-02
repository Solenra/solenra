import {AfterViewInit, Component, EventEmitter, HostListener, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {SolarSystem} from '../../core/model/solar-system';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {catchError, interval, map, merge, of, startWith, Subscription, switchMap} from 'rxjs';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {SolarSystemService} from '../../core/service/solar-system.service';
import {AuditLogDialogComponent} from '../../core/component/audit-log-dialog/audit-log-dialog.component';
import {MaterialModule} from '../../material.module';
import {CommonModule} from '@angular/common';
import {FormControlPipe} from '../../core/pipe/form-control.pipe';
import {StatusLabelComponent} from '../../core/component/status-label/status-label.component';
import {PageHeaderComponent} from '../../core/component/page-header/page-header.component';
import { EditFieldsDialogComponent } from '../../core/component/edit-fields-dialog/edit-fields-dialog.component';

@Component({
  selector: 'app-list-solar-systems',
  imports: [
    MaterialModule,
    CommonModule,
    ReactiveFormsModule,
    FormControlPipe,
    FormsModule,
    StatusLabelComponent,
    RouterLink,
    PageHeaderComponent
  ],
  templateUrl: './list-solar-systems.component.html',
  styleUrl: './list-solar-systems.component.css'
})
export class ListSolarSystemsComponent implements OnInit, AfterViewInit, OnDestroy {
  displayedColumns: string[] = ['name', 'roiAnnualised', 'status', 'action'];
  solarSystemDatabase!: SolarSystemDatabase | null;
  data: SolarSystem[] = [];

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
  //statuses: Status[];
  selectAll = false;
  rowSelections: any = {};
  selectedSolarSystems: { [key: string]: SolarSystem } = {};
  selectedSolarSystemsCount: number = 0;

  doingRefresh: boolean = false;
  reloadSubscription!: Subscription;

  access: any = {};
  selectedStatusChangeAccess: { [key: string]: boolean } = {};
  /*statusInactive = STATUS_INACTIVE;
  statusCodeReopened = STATUS_CODE_REOPENED;
  permissionCodeAuditSolarSystemRead = PERMISSION_CODE_AUDIT_SOLAR_SYSTEM_READ;*/

  constructor(
    public dialog: MatDialog,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private formBuilder: FormBuilder,
    //private dataService: DataService,
    //private userService: UserService,
    private solarSystemService: SolarSystemService
  ) {
    /*if (!this.dataService.serviceData['listSolarSystemPaging']) {
      this.dataService.serviceData['listSolarSystemPaging'] = {};
    } else {
      this.initialPageIndex = this.dataService.serviceData['listSolarSystemPaging'].pageIndex;
      this.initialPageSize = this.dataService.serviceData['listSolarSystemPaging'].pageSize;
      this.initialSortActive = this.dataService.serviceData['listSolarSystemPaging'].sortActive;
      this.initialSortDirection = this.dataService.serviceData['listSolarSystemPaging'].sortDirection;
    }*/

    if (!this.initialSortActive) {
      this.initialSortActive = "name";
    }
    if (!this.initialSortDirection) {
      this.initialSortDirection = "desc";
    }
    if (!this.initialPageSize) {
      this.initialPageSize = 10;
    }

    this.createForm();
    //this.populateFormFromUrl(this.dataService.serviceData['listSolarSystemPaging'].searchFormValue);
    this.populateFormFromUrl(null);
  }

  ngOnInit(): void {
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
    this.solarSystemDatabase = new SolarSystemDatabase(this.solarSystemService);

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
          return this.solarSystemDatabase!.searchSolarSystems(
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

          if (data.content) {
            for (const solarSystem of data.content) {
              // TODO determine access user has for each solar system
              //this.access[solarSystem.id] = this.solarSystemService.getAccess(solarSystem, this.statuses, this.userSystemPrivileges);
              if (solarSystem.solarSystemIntegrations) {
                for (const solarSystemIntegration of solarSystem.solarSystemIntegrations) {
                  if (solarSystemIntegration.status?.autoReload) {
                    doRefresh = true;
                  }
                }
              }
            }
          }

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
    /*this.dataService.serviceData['listSolarSystemPaging'].pageIndex = this.paginator.pageIndex;
    this.dataService.serviceData['listSolarSystemPaging'].pageSize = this.paginator.pageSize;
    this.dataService.serviceData['listSolarSystemPaging'].pageSize = this.paginator.pageSize;
    this.dataService.serviceData['listSolarSystemPaging'].sortActive = this.sort.active;
    this.dataService.serviceData['listSolarSystemPaging'].sortDirection = this.sort.direction;*/
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
    //this.dataService.serviceData['listSolarSystemPaging'].searchFormValue = this.searchForm.value;
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
        enabled = true;
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
        //this.dataService.serviceData['listSolarSystemPaging'].searchFormValue = this.searchForm.value;
      }
    });
  }

  selectAllChange() {
    for (let subjectOffering of this.data) {
      if (this.selectAll) {
        if (!this.rowSelections[subjectOffering.id]) {
          this.rowSelections[subjectOffering.id] = true;
        }
      } else {
        if (this.rowSelections[subjectOffering.id]) {
          this.rowSelections[subjectOffering.id] = false;
        }
      }

      this.selectionChange(subjectOffering);
    }
  }

  selectionChange(solarSystem: SolarSystem) {
    if (this.rowSelections[solarSystem.id]) {
      this.selectedSolarSystems[solarSystem.id] = solarSystem;
    } else {
      delete this.selectedSolarSystems[solarSystem.id];
    }

    this.selectedSolarSystemsCount = Object.keys(this.selectedSolarSystems).length;
  }

  openAuditLog(solarSystem: SolarSystem) {
    let auditLogDialogRef: MatDialogRef<AuditLogDialogComponent>;
    auditLogDialogRef = this.dialog.open(AuditLogDialogComponent, {
      data: {
        solarSystem: solarSystem
      },
      disableClose: true
    });
  }

  edit(solarSystem: any): void {
    this.openDialog(solarSystem);
  }

  openDialog(solarSystem?: any): void {
    const isEditing = !!solarSystem;
    const dialogRef = this.dialog.open(EditFieldsDialogComponent, {
      width: '420px',
      data: {
        title: isEditing ? 'Edit Solar System' : 'Add Solar System',
        formData: { name: '', outlayCost: null, notes: '' },
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
      this.solarSystemService.save(formData).subscribe({
        next: () => {
          dialogRef.close();
          // reload table
          this.tableDataChange.emit();
        },
        error: (err: any) => {
          console.error('Error saving solar system:', err);
        }
      });
    });
  }

}

export class SolarSystemDatabase {

  constructor(private solarSystemService: SolarSystemService) {}

  searchSolarSystems(sort: string, order: string, pageIndex: number, pageSize: number, formValues: any) {
    return this.solarSystemService.searchSolarSystems(undefined, sort, order, pageIndex, pageSize, formValues);
  }

}
