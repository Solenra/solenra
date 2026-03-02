import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DataService {

  /**
   * The object containing data.
   */
  public data: { [key: string]: any } = {};

  constructor() { }

}
