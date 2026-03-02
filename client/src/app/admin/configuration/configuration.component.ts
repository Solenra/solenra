import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { EditFieldsDialogComponent } from '../../core/component/edit-fields-dialog/edit-fields-dialog.component';
import { PageHeaderComponent } from '../../core/component/page-header/page-header.component';
import { ConfigService } from '../../core/service/config.service';

@Component({
  selector: 'app-configuration',
  imports: [
    CommonModule,
    MatButtonModule,
    MatTableModule,
    MatIconModule,
    MatPaginatorModule,
    MatSortModule,
    MatTooltipModule,
    MatCardModule,
    PageHeaderComponent
  ],
  templateUrl: './configuration.component.html',
  styleUrl: './configuration.component.css',
})
export class ConfigurationComponent implements OnInit {

  configs: any[] = [];
  displayedColumns: string[] = ['code','value','actions'];
  dataSource = new MatTableDataSource<any>([]);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  isLoading = false;

  constructor(
    private configService: ConfigService,
    public dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.configService.getAll().subscribe({
      next: (data: any) => {
        const configs = Array.isArray(data)?data:data?.content||[];
        this.configs = configs;
        this.dataSource.data = configs;
        setTimeout(() => {
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
        });
        this.isLoading = false;
      },
      error: err => {
        console.error('Error loading configs', err);
        this.isLoading = false;
      }
    });
  }

  edit(config: any): void {
    const dialogData = {
      title: 'Edit Configuration',
      formData: { code: config.code, value: config.value || '' },
      formFields: [
        { controlName: 'code', placeholder: 'Code', type: 'text', required: true, disabled: true },
        { controlName: 'value', placeholder: 'Value', type: 'text', required: false }
      ],
      longFields: false,
      saveButtonLabel: 'Save',
      saveEventLabel: 'Saving...',
      saveEventEmit: true,
      saveEventFailureEmitter: null
    };
    const dialogRef = this.dialog.open(EditFieldsDialogComponent, { width: '600px', data: dialogData });
    dialogRef.componentInstance.onSave.subscribe((updated: any) => {
      this.configService.save(config.code, updated.value).subscribe({
        next: () => { dialogRef.close(); this.load(); },
        error: e => console.error('Error saving config', e)
      });
    });
  }

}

