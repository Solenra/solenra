import {AfterViewInit, Component, Input, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {AuditLog} from '../../model/audit-log';
import {catchError, map, merge, of, startWith, switchMap} from 'rxjs';
import {SolarSystem} from '../../model/solar-system';
import {SolarSystemService} from '../../service/solar-system.service';
import { CommonModule, DatePipe } from '@angular/common';
import {MaterialModule} from '../../../material.module';
import {StatusLabelComponent} from '../status-label/status-label.component';

@Component({
  selector: 'app-audit-log',
  templateUrl: './audit-log.component.html',
  imports: [
    MaterialModule,
    CommonModule,
    DatePipe,
    StatusLabelComponent
  ],
  styleUrls: ['./audit-log.component.css']
})
export class AuditLogComponent implements AfterViewInit {

  @Input() solarSystem!: SolarSystem;

  displayedColumns: string[] = ['actionDate', 'auditUserFirstName', 'auditActionName', 'statusDisplayOrder', 'message'];
  auditLogDatabase!: AuditLogDatabase | null;
  data: AuditLog[] = [];

  resultsLength = 0;
  isLoadingDataTable = true;
  isError = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private solarSystemService: SolarSystemService
  ) { }

  ngAfterViewInit() {
    this.auditLogDatabase = new AuditLogDatabase(this.solarSystemService);

    const displayDataChanges = [
      this.sort.sortChange,
      this.paginator.page
    ];

    // If the user changes the sort order, filter, etc, reset back to the first page.
    this.sort.sortChange.subscribe(() => (this.paginator.pageIndex = 0));

    merge(...displayDataChanges)
      .pipe(
        startWith({}),
        switchMap(() => {
          this.isLoadingDataTable = true;
          return this.auditLogDatabase!.getAuditLog(
            this.sort.active,
            this.sort.direction,
            this.paginator.pageIndex,
            this.paginator.pageSize,
            this.solarSystem.id
          );
        }),
        map(data => {
          this.isLoadingDataTable = false;
          this.isError = false;

          if (data === null) {
            return [];
          }

          // TODO
          //this.resultsLength = data.totalElements;
          return [];// data.content;
        }),
        catchError((err, caught) => {
          this.isLoadingDataTable = false;
          this.isError = true;
          console.error(err);
          return of([]);
        })
      )
      .subscribe(data => (this.data = data));
  }

}

export class AuditLogDatabase {

  constructor(private solarSystemService: SolarSystemService) {}

  getAuditLog(sort: string, order: string, pageIndex: number, pageSize: number, id: number) {
    return this.solarSystemService.getAuditLog(sort, order, pageIndex, pageSize, id);
  }

}
