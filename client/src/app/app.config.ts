import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import {provideHttpClient, withInterceptors, withXhr} from '@angular/common/http';
import {errorInterceptor} from './core/interceptor/error.interceptor';
import {provideOptimus} from '@openng/optimus-ui/config';
import Aura from '@openng/optimus-ui-themes/aura'
import { authInterceptor } from './core/interceptor/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideAnimationsAsync(),
    provideHttpClient(withXhr(), withInterceptors([authInterceptor, errorInterceptor])),
    provideZoneChangeDetection(),
    provideOptimus({
      theme: {
        preset: Aura
      }
    })
  ]
};
