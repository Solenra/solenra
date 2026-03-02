import { Injectable } from '@angular/core';
import {ServerService} from './server.service';

@Injectable({
  providedIn: 'root'
})
export class SchedulerService {

  private restUrl = '/server/api/scheduler';

  constructor(private serverService: ServerService) { }

  getSchedulers() {
    return this.serverService.getRequest(this.restUrl + '/status');
  }

}
