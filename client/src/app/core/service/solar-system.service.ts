import { Injectable } from '@angular/core';
import {QueryParams, ServerService} from './server.service';
import {Observable} from 'rxjs';
import {ApiPage} from '../model/api-page';
import {SolarSystem, SystemEnergyDetails} from '../model/solar-system';

@Injectable({
  providedIn: 'root'
})
export class SolarSystemService {

  private restUrl = '/server/api/solar-system';

  constructor(private serverService: ServerService) { }

  setPagingParams(params: QueryParams, sort?: string | null, order?: string | null, pageIndex?: number | null, pageSize?: number | null) {
    if (sort) {
      params['sort'] = sort + ':' + (order || 'asc');
    }
    if (pageIndex) {
      params['pageIndex'] = String(pageIndex);
    }
    if (pageSize) {
      params['pageSize'] = String(pageSize);
    }
    return params;
  }

  removeNull<T>(obj: T | any) {
    Object.keys(obj).forEach((key) => {
      if (obj[key] && typeof obj[key] === 'object') {
        this.removeNull(obj[key]);
      } else if (obj[key] == null) {
        delete obj[key];
      }
    });
    return obj;
  }

  searchSolarSystems(solarSystemId: number | undefined, sort?: string, order?: string, pageIndex?: number, pageSize?: number, formValues?: any): Observable<ApiPage<SolarSystem>> {
    let params: QueryParams = {};
    params = this.setPagingParams(params, sort, order, pageIndex, pageSize);
    if (formValues) {
      params = {...params, ...formValues}
    }

    if (solarSystemId) {
      params['solarSystemId'] = String(solarSystemId);
    }

    // remove null values
    params = this.removeNull(params)

    return this.serverService.getRequest(this.restUrl + '/search', params);
  }

  searchSolarSystemEnergyDetails(solarSystemId: number, sort: string, order: string, pageIndex: number, pageSize: number, formValues: any): Observable<ApiPage<SystemEnergyDetails>> {
    let params: QueryParams = {};
    params = this.setPagingParams(params, sort, order, pageIndex, pageSize);
    if (formValues) {
      params = {...params, ...formValues}
    }

    if (solarSystemId) {
      params['solarSystemId'] = String(solarSystemId);
    }

    // remove null values
    params = this.removeNull(params)

    return this.serverService.getRequest(this.restUrl + '/search-energy-details', params);
  }

  save(solarSystem: any): Observable<any> {
    return this.serverService.postRequest(this.restUrl + '/save', solarSystem);
  }

  saveIntegration(solarSystemId: number, integrationData: any): Observable<any> {
    return this.serverService.postRequest(this.restUrl + '/' + solarSystemId + '/integration', integrationData);
  }

  deleteIntegration(solarSystemId: number, integrationCode: string): Observable<any> {
    return this.serverService.deleteRequest(this.restUrl + '/' + solarSystemId + '/integration/' + integrationCode);
  }

  getAuditLog(sort: string, order: string, pageIndex: number, pageSize: number, id: number): any {
    // TODO
  }

}
