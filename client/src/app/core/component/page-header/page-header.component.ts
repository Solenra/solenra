import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-page-header',
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.css']
})
export class PageHeaderComponent {
  @Input() title: string = '';
  @Input() buttonLabel: string = '';
  @Input() buttonIcon: string = 'add';
  @Input() onAddClick?: () => void;
  @Input() showBackButton: boolean = false;
  @Input() onBackClick?: () => void;
  @Input() showImport: boolean = false;
  @Input() showExport: boolean = false;
  @Input() onImportClick?: () => void;
  @Input() onExportClick?: () => void;
}
