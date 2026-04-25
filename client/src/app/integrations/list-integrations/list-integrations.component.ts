import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { PageHeaderComponent } from '../../core/component/page-header/page-header.component';
import { EditFieldsDialogComponent } from '../../core/component/edit-fields-dialog/edit-fields-dialog.component';
import { IntegrationService } from '../../core/service/integration.service';

@Component({
  selector: 'app-list-integrations',
  imports: [
    CommonModule,
    MatButtonModule,
    MatTableModule,
    MatIconModule,
    MatPaginatorModule,
    MatSortModule,
    MatTooltipModule,
    MatCheckboxModule,
    PageHeaderComponent
  ],
  templateUrl: './list-integrations.component.html',
  styleUrl: './list-integrations.component.css',
})
export class ListIntegrationsComponent implements OnInit {

  integrations: any[] = [];
  displayedColumns: string[] = ['name', 'code', 'enabled', 'actions'];
  dataSource = new MatTableDataSource<any>([]);

  resultsLength = 0;
  isLoadingDataTable = false;
  initialPageIndex = 0;
  initialPageSize = 10;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private integrationService: IntegrationService,
    public dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoadingDataTable = true;
    this.integrationService.getAll().subscribe({
      next: (data: any) => {
        const integrations = (Array.isArray(data) ? data : data?.content) || [];
        this.integrations = integrations;
        this.dataSource.data = integrations;
        // wire paginator and sort
        setTimeout(() => {
          this.dataSource.paginator = this.paginator;
          if (this.sort) { this.dataSource.sort = this.sort; }
        });
        this.resultsLength = integrations.length;
        this.isLoadingDataTable = false;
      },
      error: (err: any) => {
        console.error('Error loading integrations:', err);
        this.isLoadingDataTable = false;
      }
    });
  }

  edit(integration: any): void {
    let formData: {[index: string]:any} = { 
      name: integration.name,
      enabled: integration.enabled
    };

    let formFields: any[] = [
      { controlName: 'name', label: 'Name', type: 'text', required: true, disabled: true },
      { controlName: 'enabled', label: 'Enabled', type: 'boolean', required: false }
    ];

    for (const credential of integration.credentials.sort((a: any, b: any) => a.type.localeCompare(b.type))) {
      formData[credential.type] = credential.value;
      formFields.push({ controlName: credential.type, label: credential.type, type: 'text', required: false, disabled: false });
    }

    const dialogData = {
      title: 'Edit Integration',
      formData: formData,
      formFields: formFields,
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
      formData.code = integration.code;
      this.integrationService.save(formData).subscribe({
        next: () => {
          dialogRef.close();
          this.load();
        },
        error: (err: any) => {
          console.error('Error saving integration:', err);
        }
      });
    });
  }

}
