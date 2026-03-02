import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent, HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { catchError, filter, switchMap, take, tap, finalize } from 'rxjs/operators';
import { jwtDecode } from 'jwt-decode';

interface TokenResponse {
  accessToken: string;
}

const isRefreshing$ = new BehaviorSubject<boolean>(false);
const accessTokenSubject$ = new BehaviorSubject<string | null>(null);

export const jwtInterceptor: HttpInterceptorFn = (
  req: HttpRequest<any>,
  next: HttpHandlerFn
): Observable<HttpEvent<any>> => {
  const http = inject(HttpClient);

  const isLoginRequest = req.url.includes('/api/identity/login');
  const isTokenRefreshRequest = req.url.includes('/server/api/identity/token/refresh');
  const accessToken = localStorage.getItem('accessToken');

  // If token exists and it's not a login request
  if (accessToken && !isLoginRequest && !isTokenRefreshRequest) {
    if (isTokenExpired(accessToken)) {
      return handle401Refresh(req, next, http);
    } else {
      req = addToken(req, accessToken);
    }
  }

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !req.url.includes('/server/api/identity/token/refresh')) {
        return handle401Refresh(req, next, http);
      }
      return throwError(() => err);
    })
  );
};

function addToken(req: HttpRequest<any>, token: string): HttpRequest<any> {
  return req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });
}

function isTokenExpired(token: string): boolean {
  try {
    const decoded: any = jwtDecode(token);
    const expiry = decoded.exp * 1000;
    return expiry < Date.now();
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
    return throwError(() => new Error('No refresh token available'));
  }

  if (!isRefreshing$.value) {
    isRefreshing$.next(true);
    accessTokenSubject$.next(null);

    return http.post<TokenResponse>('/server/api/identity/token/refresh', { refreshToken: refreshToken }).pipe(
      tap(response => {
        localStorage.setItem('accessToken', response.accessToken);
        accessTokenSubject$.next(response.accessToken);
      }),
      switchMap(response => {
        const newReq = addToken(req, response.accessToken);
        return next(newReq);
      }),
      catchError(err => throwError(() => err)),
      finalize(() => isRefreshing$.next(false))
    );
  } else {
    return accessTokenSubject$.pipe(
      filter(token => token !== null),
      take(1),
      switchMap(token => {
        const newReq = addToken(req, token!);
        return next(newReq);
      })
    );
  }
}
