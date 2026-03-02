import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MaterialModule } from '../../material.module';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormControlPipe } from '../../core/pipe/form-control.pipe';
import { RouterLink } from '@angular/router';
import { PageHeaderComponent } from '../../core/component/page-header/page-header.component';

@Component({
  selector: 'app-registration',
  imports: [
    MaterialModule,
    CommonModule,
    ReactiveFormsModule,
    FormControlPipe,
    FormsModule,
    RouterLink,
    PageHeaderComponent
  ],
  templateUrl: './registration.component.html',
  styleUrl: './registration.component.css'
})
export class RegistrationComponent implements OnInit {
  
  formGroup: any;
  hasTermsAndConditions = false;
  hasPrivacyPolicy = false;

  constructor(private fb: FormBuilder) {
   this.formGroup = this.fb.group({
      username: ["", {
        validators: [Validators.required]
      }],
      email: ["", {
        validators: [Validators.required, Validators.email]
      }],
      password: ['', [Validators.required, Validators.minLength(8)]],
      acceptance: ['', [Validators.required, Validators.requiredTrue]]
   });
  }

  ngOnInit(): void {
    throw new Error('Method not implemented.');
  }

  openTermsAndConditions() {
    // TODO open confirmation dialog with text

  }

  openPrivacyPolicy() {
    // TODO open confirmation dialog with text
  }

}
