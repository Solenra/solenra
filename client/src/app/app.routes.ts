import { Routes } from '@angular/router';
import { IndexComponent } from './core/component/index/index.component';
import { LayoutDefaultComponent } from './core/component/layout-default/layout-default.component';
import { PageNotFoundComponent } from './core/component/page-not-found/page-not-found.component';
import {ListSolarSystemsComponent} from './solar-system/list-solar-systems/list-solar-systems.component';
import {ViewSolarSystemComponent} from './solar-system/view-solar-system/view-solar-system.component';
import {ListEnergyComponent} from './solar-system/list-energy/list-energy.component';
import {LoginComponent} from './user/login/login.component';
import {SchedulersComponent} from './admin/schedulers/schedulers.component';
import {ListEnergyPlansComponent} from './energy-plan/list-energy-plans/list-energy-plans.component';
import {EditRatePeriodsComponent} from './energy-plan/edit-rate-periods/edit-rate-periods.component';
import { RegistrationComponent } from './user/registration/registration.component';
import { ListIntegrationsComponent } from './integrations/list-integrations/list-integrations.component';
import { DashboardComponent } from './admin/dashboard/dashboard.component';
import { ConfigurationComponent } from './admin/configuration/configuration.component';

export const routes: Routes = [
{
    path: '',
    component: LayoutDefaultComponent,
    children: [
      {
        path: '',
        component: IndexComponent
      },
      {
        path: 'login',
        component: LoginComponent
      },
      {
        path: 'register',
        component: RegistrationComponent
      },
      {
        path: 'energy-plans', component: ListEnergyPlansComponent
      },
      {
        path: 'energy-plans/:id/rates', component: EditRatePeriodsComponent
      },
      {
        path: 'solar-systems', component: ListSolarSystemsComponent
      },
      {
        path: 'solar-systems/:id', component: ViewSolarSystemComponent
      },
      {
        path: 'solar-systems/:id/energy', component: ListEnergyComponent
      },
      {
        path: 'admin', component: DashboardComponent
      },
      {
        path: 'admin/configuration', component: ConfigurationComponent
      },
      {
        path: 'admin/integrations', component: ListIntegrationsComponent
      },
      {
        path: 'admin/schedulers', component: SchedulersComponent
      },
      {
        path: 'admin/users', component: PageNotFoundComponent
      }
    ]
  },
  {
    path: '**',
    component: LayoutDefaultComponent,
    children: [
      {
        path: '', component: PageNotFoundComponent
      }
    ]
  }
];
