import { Injectable } from '@angular/core';
import { ServerService } from './server.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ConfigService {
  constructor(private server: ServerService) {}

  getAll(): Observable<any> {
    return this.server.getRequest('/server/api/config/');
  }

  save(code: string, value: string): Observable<any> {
    return this.server.postRequest(`/server/api/config/${code}`, value);
  }
}
