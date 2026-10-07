import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DataService } from '../service/data.service';
import { ServerErrorComponent } from '../component/server-error/server-error.component';

export const errorInterceptor: HttpInterceptorFn = (
  req: HttpRequest<any>,
  next: HttpHandlerFn
): Observable<HttpEvent<any>> => {
  const dialog = inject(MatDialog);
  const dataService = inject(DataService);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        const errorMessage = extractErrorMessage(error);
        const previousStatus = dataService.data['server-error-dialog-status'];
        const isDialogOpen = dataService.data['server-error-dialog-open'];

        // Display popup if not already open for this status code
        if (!isDialogOpen || previousStatus !== error.status) {
          dataService.data['server-error-dialog-open'] = true;
          dataService.data['server-error-dialog-status'] = error.status;

          const dialogRef = dialog.open(ServerErrorComponent, {
            data: { errorMessage, error },
            disableClose: true
          });

          dialogRef.afterClosed().subscribe(() => {
            dataService.data['server-error-dialog-open'] = false;
            delete dataService.data['server-error-dialog-status'];
          });
        }
      }

      console.error(error);
      return throwError(() => error);
    })
  );
};

function extractErrorMessage(error: HttpErrorResponse): string {
  if (!error.error) return `${error.status} - ${error.statusText || 'Unknown Error'}`;

  if (typeof error.error === 'object') {
    if (error.error.message) return error.error.message;
    if (error.error.detail) return error.error.detail;
    if (error.error.title) return error.error.title;
  }

  if (typeof error.error === 'string') return error.error;

  return `${error.status} - ${error.statusText || 'Server Error'}`;
}