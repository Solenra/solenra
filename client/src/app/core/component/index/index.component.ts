import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatToolbarModule } from '@angular/material/toolbar';

@Component({
  selector: 'app-index',
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatCardModule,
    MatGridListModule,
  ],
  templateUrl: './index.component.html',
  styleUrl: './index.component.css'
})
export class IndexComponent {
  features = [
    { title: 'ROI & Payback Calculator', description: 'Track how long your solar investment takes to pay for itself.' },
    { title: 'Historical ROI Comparison', description: 'Compare your system’s ROI across different time periods.' },
    { title: 'Export Tariff Revenue', description: 'See how much you earn from feeding energy back to the grid.' },
    { title: 'Self-Consumption Savings', description: 'Understand the savings from using your solar energy directly.' }
  ];
}
