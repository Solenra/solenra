import {Component, OnInit} from '@angular/core';
import {SchedulerService} from '../../core/service/scheduler.service';
import {Scheduler} from '../../core/model/scheduler';
import {MaterialModule} from '../../material.module';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {PageHeaderComponent} from '../../core/component/page-header/page-header.component';

@Component({
  selector: 'app-schedulers',
  imports: [
    MaterialModule,
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    PageHeaderComponent
  ],
  templateUrl: './schedulers.component.html',
  styleUrl: './schedulers.component.css'
})
export class SchedulersComponent implements OnInit {

  runningJobsDisplayedColumns: string[] = ['name', 'group', 'triggerName', 'triggerGroup', 'triggerJobData', 'triggerStartTime'];
  schedulersLoading = true;
  schedulers: Scheduler[] = [];

  constructor(
    public schedulerService: SchedulerService) {
  }

  ngOnInit(): void {
    this.loadSchedulers();
  }

  loadSchedulers() {
    this.schedulersLoading = true;
    this.schedulerService.getSchedulers().subscribe({
      next: (returnedSchedulers: any[]) => {
        this.schedulers = returnedSchedulers.sort((a, b) => a.schedulerName.localeCompare(b.schedulerName));
        this.schedulersLoading = false;
      },
      error: (err: any) => {
        this.schedulersLoading = false;
      },
      complete: () => { }
    });
  }

}
