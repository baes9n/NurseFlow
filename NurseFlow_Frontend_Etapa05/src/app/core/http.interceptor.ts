import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';
export const authInterceptor:HttpInterceptorFn=(request,next)=>{
  const auth=inject(AuthService),router=inject(Router), token=auth.token();
  const req=token && request.url.startsWith('/api/v1/') ? request.clone({setHeaders:{Authorization:`Bearer ${token}`}}):request;
  return next(req).pipe(catchError((error:unknown)=>{
    if(error instanceof HttpErrorResponse && error.status===401 && !request.url.endsWith('/auth/login')) {
      auth.clear(); void router.navigateByUrl('/login');
    }
    return throwError(()=>error);
  }));
};
