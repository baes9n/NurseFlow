import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pagina } from './models';
// Con proxy en desarrollo, siempre se utiliza /api/v1 (mismo origen).
export type Query = Record<string, string | number | boolean | null | undefined>;
@Injectable({providedIn:'root'})
export class ApiService {
  private http = inject(HttpClient);
  private url(path:string) { return '/api/v1/' + path.replace(/^\//,''); }
  private params(query:Query):HttpParams {
    let params = new HttpParams();
    Object.entries(query).forEach(([key,val])=>{if (val!==null && val!==undefined && val!=='') params=params.set(key,String(val));});
    return params;
  }
  get<T>(path:string, query:Query={}) {return this.http.get<T>(this.url(path),{params:this.params(query)});}
  page<T>(path:string, query:Query={}) {return this.get<Pagina<T>>(path,query);}
  post<T>(path:string, body:unknown={}) {return this.http.post<T>(this.url(path),body);}
  put<T>(path:string, body:unknown) {return this.http.put<T>(this.url(path),body);}
  patch<T>(path:string, body:unknown={}) {return this.http.patch<T>(this.url(path),body);}
  download(path:string, query:Query={}) {return this.http.get(this.url(path),{params:this.params(query),responseType:'blob'});}
  delete(path:string) {return this.http.delete<void>(this.url(path));}
}
