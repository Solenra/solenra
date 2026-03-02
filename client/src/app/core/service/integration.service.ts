import { Injectable } from '@angular/core';
import { ServerService } from './server.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class IntegrationService {

  constructor(private serverService: ServerService) { }

  getAll(): Observable<any> {
    return this.serverService.getRequest('/server/api/integration/list');
  }

  save(integration: any): Observable<any> {
    return this.serverService.postRequest('/server/api/integration', integration);
  }

}
