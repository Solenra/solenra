import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormControl, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { IntegrationService } from '../../core/service/integration.service';

@Component({
  selector: 'app-connect-integration-dialog',
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    MatIconModule
  ],
  templateUrl: './connect-integration-dialog.component.html',
  styleUrls: ['./connect-integration-dialog.component.css']
})
export class ConnectIntegrationDialogComponent {

  form: FormGroup;
  integrations: any[] = [];
  timezones: string[] = [];
  filteredTimezones: string[] = [];
  timezoneFilter: FormControl = new FormControl('');

  constructor(
    private fb: FormBuilder,
    private integrationService: IntegrationService,
    public dialogRef: MatDialogRef<ConnectIntegrationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.form = this.fb.group({
      integrationCode: ['', Validators.required],
      timezone: ['', Validators.required],
      systemId: [''],
      apiKey: ['']
    });

    this.timezoneFilter.valueChanges.subscribe(value => {
      this.applyTimezoneFilter(value);
    });

    // Load available integrations
    this.integrationService.getAll().subscribe({
      next: (integrations: any[]) => {
        // Filter to only enabled/active integrations
        this.integrations = integrations.filter((integration: any) => integration.enabled);
      },
      error: (err: any) => {
        console.error('Error loading integrations:', err);
      }
    });

    this.integrationService.getTimezones().subscribe({
      next: (timezones: string[]) => {
        this.timezones = timezones;
        this.filteredTimezones = [...timezones];
      },
      error: (err: any) => {
        console.error('Error loading timezones', err);
      }
    });

    // Watch for changes to integration selection
    this.form.get('integrationCode')?.valueChanges.subscribe(code => {
      this.updateSystemIdRequirement(code);
    });
  }

  private updateSystemIdRequirement(code: string) {
    const systemIdControl = this.form.get('systemId');
    const apiKeyControl = this.form.get('apiKey');
    if (code === 'solaredge_v1') {
      systemIdControl?.setValidators([Validators.required]);
      apiKeyControl?.setValidators([Validators.required]);
    } else {
      systemIdControl?.clearValidators();
      apiKeyControl?.clearValidators();
    }
    systemIdControl?.updateValueAndValidity();
    apiKeyControl?.updateValueAndValidity();
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onConnect(): void {
    if (this.form.valid) {
      const formValue = this.form.value;
      const result: { code: string; 'system-id'?: string; 'api-key'?: string; timezone: string } = {
        code: formValue.integrationCode,
        timezone: formValue.timezone
      };

      if (formValue.integrationCode === 'solaredge_v1') {
        result['system-id'] = formValue.systemId;
        result['api-key'] = formValue.apiKey;
      }
      result['timezone'] = formValue.timezone;

      this.dialogRef.close(result);
    }
  }

  isSystemIdRequired(): boolean {
    return this.form.get('integrationCode')?.value === 'solaredge_v1';
  }

  isApiKeyRequired(): boolean {
    return this.form.get('integrationCode')?.value === 'solaredge_v1';
  }

  applyTimezoneFilter(filter: string): void {
    const normalizedFilter = filter ? filter.toLowerCase() : '';
    this.filteredTimezones = this.timezones.filter(timezone => timezone.toLowerCase().includes(normalizedFilter));
  }

}