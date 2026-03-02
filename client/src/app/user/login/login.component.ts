import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {IdentityService} from '../../core/service/identity.service';

import {MaterialModule} from '../../material.module';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationDialogComponent} from '../../core/component/confirmation-dialog/confirmation-dialog.component';
import {FormControlPipe} from '../../core/pipe/form-control.pipe';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    MaterialModule,
    FormControlPipe
],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  submitted = false;

  constructor(
    public dialog: MatDialog,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private identityService: IdentityService
  ) { }

  ngOnInit() {
    this.form = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    this.submitted = true;

    if (this.form.invalid) {
      return;
    }

    this.loading = true;
    this.identityService.login(this.form.controls['username'].value, this.form.controls['password'].value)
      .subscribe({
        next: async (tokens) => {
          localStorage.setItem('accessToken', tokens['accessToken']);
          localStorage.setItem('refreshToken', tokens['refreshToken']);

          await this.identityService.loadIdentity();

          // get return url from query parameters or default to home page
          const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
          this.router.navigateByUrl(returnUrl);
        },
        error: (error: any) => {
          this.loading = false;

          let errorMessage = 'Please check your credentials and try again.';

          if (error.status !== 403 && error.error) {
            errorMessage = error.error.error + ': ' + error.error.message;
          }

          const confirmDialogRef = this.dialog.open(ConfirmationDialogComponent, {
            data: {
              title: 'Authentication error',
              warning: errorMessage,
              cancelLabel: 'Close'
            },
            disableClose: true
          });
        },
        complete: () => { }
      });
  }

}
