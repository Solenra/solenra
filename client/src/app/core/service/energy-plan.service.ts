import { Injectable } from '@angular/core';
import {ServerService} from './server.service';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EnergyPlanService {

  private restUrl = '/server/api/energy-plan';

  constructor(private serverService: ServerService) { }

  getAll(): Observable<any> {
    return this.serverService.getRequest(this.restUrl + '/search');
  }

  get(id: number): Observable<any> {
    return this.serverService.getRequest(this.restUrl + '/get', { id: String(id) });
  }

  save(energyPlan: any): Observable<any> {
    return this.serverService.postRequest(this.restUrl + '/save', energyPlan);
  }

  delete(id: number): Observable<any> {
    return this.serverService.getRequest(this.restUrl + '/delete', { id: String(id) });
  }

}
