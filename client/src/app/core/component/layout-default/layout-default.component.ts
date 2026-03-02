import { Component } from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {MenuItemsComponent} from '../menu-items/menu-items.component';
import {MaterialModule} from '../../../material.module';

@Component({
  selector: 'app-layout-default',
  imports: [
    RouterOutlet,
    MaterialModule,
    MenuItemsComponent
],
  templateUrl: './layout-default.component.html',
  styleUrl: './layout-default.component.css'
})
export class LayoutDefaultComponent {

}
