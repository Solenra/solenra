import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpParams} from '@angular/common/http';
import {MatDialog} from '@angular/material/dialog';
import {catchError, Observable, of, throwError} from 'rxjs';
import {ServerErrorComponent} from '../component/server-error/server-error.component';
import {DataService} from './data.service';

export type QueryParams = {
  [param: string]: string | readonly string[]
}

@Injectable({
  providedIn: 'root'
})
export class ServerService {

  constructor(
    private httpClient: HttpClient,
    public dialog: MatDialog,
    public dataService: DataService
  ) { }

  /*
   * Making get request
   */
  public getRequest(url: string, urlSearchParams: QueryParams = {}): any {
    return this.doGetRequest(url, urlSearchParams, true, undefined);
  }

  /*
   * Making get request
   */
  public getRequestMain(url: string, urlSearchParams: QueryParams = {}, displayErrorPopup: boolean, ignoreErrorHttpCodes?: number[]): any {
    return this.doGetRequest(url, urlSearchParams, displayErrorPopup, ignoreErrorHttpCodes);
  }

  doGetRequest(url: string, urlSearchParams: QueryParams = {}, displayErrorPopup: boolean, ignoreErrorHttpCodes?: number[]) {
    const httpHeaders = new HttpHeaders()
      .set('Cache-Control', 'no-cache')
      .set('Pragma', 'no-cache')
      .set('Expires', 'Sat, 01 Jan 2000 00:00:00 GMT');

    return this.httpClient.get(url, {
      params: new HttpParams({fromObject: urlSearchParams}),
      headers: httpHeaders,
      responseType: 'json',
    })
      .pipe(
        catchError((error: HttpErrorResponse) =>
          this.handleError(error, displayErrorPopup, ignoreErrorHttpCodes)
        )
      );
  }

  public postRequest(url: string, data: any, httpParams?: HttpParams): Observable<any> {
    return this.doPostRequest(url, data, httpParams, true);
  }

  public doPostRequest(url: string, data: any, httpParams: HttpParams | undefined, displayErrorPopup: boolean): Observable<any> {
    const httpHeaders = new HttpHeaders()
      .set('Content-type', 'application/json');

    const requestOptions = {
      params: httpParams,
      headers: httpHeaders
    };

    return this.httpClient.post(url, data, requestOptions)
      .pipe(
        catchError((error: HttpErrorResponse) =>
          this.handleError(error, displayErrorPopup)
        )
      );
  }

  public putRequest(url: string, data: any, httpParams?: HttpParams): any {
    const httpHeaders = new HttpHeaders()
      .set('Content-type', 'application/json');

    const requestOptions = {
      params: httpParams,
      headers: httpHeaders
    };

    return this.httpClient.put<any>(url, data, requestOptions)
      .pipe(
        catchError((error: HttpErrorResponse) =>
          this.handleError(error, true)
        )
      );
  }

  public deleteRequest(url: string, httpParams?: HttpParams): any {
    const httpHeaders = new HttpHeaders()
      .set('Content-type', 'application/json');

    const requestOptions = {
      params: httpParams,
      headers: httpHeaders
    };

    return this.httpClient.delete<any>(url, requestOptions)
      .pipe(
        catchError((error: HttpErrorResponse) =>
          this.handleError(error, true)
        )
      );
  }

  private handleError(error: HttpErrorResponse | any, displayErrorPopup: boolean, ignoreErrorHttpCodes?: number[]) {

    let errorMessage: string;
    if (error instanceof HttpErrorResponse) {
      errorMessage = error.status + ' - ' + (error.statusText || '');

      this.dataService.data['server-error-dialog-status'] = error.status;

      if (displayErrorPopup
        && (!this.dataService.data['server-error-dialog-open'] || this.dataService.data['server-error-dialog-status'] !== error.status)
        && (!ignoreErrorHttpCodes || !ignoreErrorHttpCodes.includes(error.status))
      ) {
        this.dataService.data['server-error-dialog-open'] = true;
        const dialogRef = this.dialog.open(ServerErrorComponent, {
          data: {
            errorMessage,
            error
          },
          disableClose: true
        });
      }
    } else {
      errorMessage = error.message ? error.message : error.toString();
    }

    console.error(error);
    return throwError(error);
  }

}
