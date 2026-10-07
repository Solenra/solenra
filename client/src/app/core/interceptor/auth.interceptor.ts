import { HttpBackend, HttpClient, HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, finalize, map, shareReplay, switchMap, tap } from 'rxjs/operators';
import { jwtDecode } from 'jwt-decode';

interface TokenResponse {
  accessToken: string;
}

let refreshRequest$: Observable<string> | null = null;

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<any>,
  next: HttpHandlerFn
): Observable<HttpEvent<any>> => {
  const http = new HttpClient(inject(HttpBackend));

  const isLoginRequest = req.url.includes('/api/identity/login');
  const isTokenRefreshRequest = req.url.includes('/server/api/identity/token/refresh');
  const accessToken = localStorage.getItem('accessToken');

  if (accessToken && !isLoginRequest && !isTokenRefreshRequest) {
    if (isTokenExpired(accessToken)) {
      return handle401Refresh(req, next, http);
    } else {
      req = addToken(req, accessToken);
    }
  }

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      // Catch 401s and attempt refresh
      if (err.status === 401 && !isTokenRefreshRequest && !isLoginRequest) {
        return handle401Refresh(req, next, http);
      }
      return throwError(() => err);
    })
  );
};

function addToken(req: HttpRequest<any>, token: string): HttpRequest<any> {
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

function isTokenExpired(token: string): boolean {
  try {
    const decoded: any = jwtDecode(token);
    return (decoded.exp * 1000) <= (Date.now() + 30_000);
  } catch {
    return true;
  }
}

function handle401Refresh(
  req: HttpRequest<any>,
  next: HttpHandlerFn,
  http: HttpClient
): Observable<HttpEvent<any>> {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) {
    return throwError(() => new HttpErrorResponse({ status: 401, statusText: 'No refresh token available.' }));
  }

  if (!refreshRequest$) {
    refreshRequest$ = http.post<TokenResponse>('/server/api/identity/token/refresh', { refreshToken }).pipe(
      tap(response => localStorage.setItem('accessToken', response.accessToken)),
      map(response => response.accessToken),
      catchError(error => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        return throwError(() => error);
      }),
      finalize(() => refreshRequest$ = null),
      shareReplay({ bufferSize: 1, refCount: false })
    );
  }

  return refreshRequest$.pipe(
    switchMap(token => next(addToken(req, token)))
  );
}