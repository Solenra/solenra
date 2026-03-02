import { Injectable } from '@angular/core';
import {BehaviorSubject, firstValueFrom, Observable, Subject} from 'rxjs';
import {Identity} from '../model/identity';
import {ServerService} from './server.service';
import {HttpParams} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class IdentityService {

  private restUrl = '/server/api/identity';

  /**
   * The BehaviorSubject containing the user details and system privileges.
   */
  public identity: Subject<Identity> = new BehaviorSubject<Identity>(null!);

  constructor(private serverService: ServerService) {
    this.loadInitialData();
  }

  private loadInitialData() {
    this.loadIdentity();
  }

  async loadIdentity(): Promise<void> {
    const returnedIdentity: Identity = await firstValueFrom(
      this.serverService.getRequestMain(`${this.restUrl}/`, null!, true, undefined)
    );
    this.identity.next(returnedIdentity);
  }

  getIdentity(): Subject<Identity> {
    return this.identity;
  }

  login(username: string, password: string): Observable<any> {
    let httpParams = new HttpParams();
    httpParams = httpParams.set('username', username);
    httpParams = httpParams.set('password', password);
    return this.serverService.doPostRequest(`${this.restUrl}/login`, null!, httpParams, false);
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }

}
