import {Component, Input, OnInit} from '@angular/core';
import {SolarSystemIntegration, Status} from '../../model/solar-system';
import {MaterialModule} from '../../../material.module';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-status-label',
  templateUrl: './status-label.component.html',
  imports: [
    MaterialModule,
    CommonModule
  ],
  styleUrls: ['./status-label.component.css']
})
export class StatusLabelComponent implements OnInit {

  @Input() status!: Status;
  @Input() autoReload!: boolean;
  @Input() additionalLabelText!: string | null;
  @Input() solarSystemIntegration!: SolarSystemIntegration;
  percentLoaded: number = 0;

  ngOnInit(): void {
    if (this.solarSystemIntegration && !this.status) {
      this.status = this.solarSystemIntegration.status;

      if (this.solarSystemIntegration.systemDetails.installationDate
        && this.solarSystemIntegration.latestLoadedDate
        && this.status.code === 'loading-from-integration-processing')
      {
        let startDate = new Date(this.solarSystemIntegration.systemDetails.installationDate);
        let latestLoadedDate = new Date(this.solarSystemIntegration.latestLoadedDate);
        let endDate = new Date(new Date().getTime() + (1000 * 60 * 60 * 24));
        this.percentLoaded = (latestLoadedDate.getTime() - startDate.getTime()) / (endDate.getTime() - startDate.getTime()) * 100;
      }
    }
  }

}
