import {Component, OnInit} from '@angular/core';
import {MatTreeNestedDataSource} from '@angular/material/tree';
import {CommonModule} from '@angular/common';
import {IdentityService} from '../../service/identity.service';
import {MaterialModule} from '../../../material.module';
import {Router, RouterLink, RouterLinkActive} from '@angular/router';

interface MenuItem {
  name: string;
  routerLink?: string;
  href?: string;
  externalLink?: boolean;
  action?: string;
  displayOrder: number;
  children?: MenuItem[];
}

const DEFAULT_ANON_MENU: MenuItem[] = [
  {
    name: 'Home',
    routerLink: '/',
    displayOrder: 10
  },
  {
    name: 'Login',
    routerLink: '/login',
    displayOrder: 20
  }/*,
  {
    name: 'Register',
    routerLink: '/register',
    displayOrder: 30
  }*/
];
const DEFAULT_USER_MENU: MenuItem[] = [
  {
    name: 'Home',
    routerLink: '/',
    displayOrder: 10
  },
  {
    name: 'Energy Plans',
    routerLink: '/energy-plans',
    displayOrder: 100
  },
  {
    name: 'Solar Systems',
    routerLink: '/solar-systems',
    displayOrder: 200
  },
  /*{
    name: 'User Details',
    routerLink: '/user-details',
    displayOrder: 500
  },*/
  {
    name: 'Logout',
    action: 'logout',
    displayOrder: 1000
  }
];
const ADMIN_MENU: MenuItem = {
  name: 'Administration',
  routerLink: '/admin',
  displayOrder: 900
};

@Component({
  selector: 'app-menu-items',
  imports: [
    MaterialModule,
    CommonModule,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './menu-items.component.html',
  styleUrl: './menu-items.component.css'
})
export class MenuItemsComponent implements OnInit {
  dataSource = new MatTreeNestedDataSource<MenuItem>();
  loadingMenu = true;

  constructor(
    private router: Router,
    private identityService: IdentityService
  ) { }

  childrenAccessor = (node: MenuItem) => node.children ?? [];
  hasChild = (_: number, node: MenuItem) => !!node.children && node.children.length > 0;

  ngOnInit(): void {
    this.loadMenu();
  }

  loadMenu() {
    this.identityService.getIdentity().subscribe(returnedIdentity => {
      console.log(' > returnedIdentity: ', returnedIdentity);
      let menuItems = [];
      if (returnedIdentity) {
        menuItems = DEFAULT_USER_MENU;
        /*if (returnedUserSystemPrivileges.includes(PERMISSION_CODE_INTEGRATIONS_READ)) {
          menuItems = this.addMenuItem(menuItems, INTEGRATIONS_MENU);
        }
        if (returnedUserSystemPrivileges.includes(PERMISSION_CODE_USER_ROLE_READ)) {
          menuItems = this.addMenuItem(menuItems, USER_MENU);
        }
        if (returnedUserSystemPrivileges.includes(PERMISSION_CODE_SCHEDULER)) {
          menuItems = this.addMenuItem(menuItems, SCHEDULER_MENU);
        }*/
        menuItems = this.addMenuItem(menuItems, ADMIN_MENU);
      }  else {
        menuItems = DEFAULT_ANON_MENU;
      }
      this.dataSource.data = menuItems.sort((a, b) => a.displayOrder - b.displayOrder);

      this.loadingMenu = false;
    });
  }

  addMenuItem(menuItems: MenuItem[], menuItem: MenuItem): MenuItem[] {
    if (!menuItems.includes(menuItem, 0)) {
      menuItems.push(menuItem);
    }
    return menuItems;
  }

  async doAction(action: string) {
    switch (action) {
      case 'logout':
        this.identityService.logout();
        await this.identityService.loadIdentity();
        this.loadMenu();
        this.router.navigateByUrl('/');
        break;
    }
  }

}
