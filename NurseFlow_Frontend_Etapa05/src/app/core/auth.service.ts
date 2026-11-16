import { Injectable, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ApiService } from './api.service';
import { TokenRespuesta, Usuario } from './models';
const TOKEN = 'nurseflow_token', USER='nurseflow_usuario';
function cachedUser(): Usuario|null {try {return JSON.parse(sessionStorage.getItem(USER)||'null') as Usuario|null;}catch{return null;}}
@Injectable({providedIn:'root'})
export class AuthService {
  private api=inject(ApiService); private router=inject(Router);
  user=signal<Usuario|null>(cachedUser());
  token=signal<string|null>(sessionStorage.getItem(TOKEN));
  isAdmin=computed(()=>this.user()?.rol==='ADMIN');
  logged=computed(()=>!!this.token() && !!this.user());
  async login(correo:string,contrasena:string):Promise<void> {
    const response=await firstValueFrom(this.api.post<TokenRespuesta>('auth/login',{correo,contrasena}));
    sessionStorage.setItem(TOKEN,response.accessToken);
    sessionStorage.setItem(USER,JSON.stringify(response.usuario));
    this.token.set(response.accessToken);this.user.set(response.usuario);
    await this.router.navigateByUrl('/app/dashboard');
  }
  async refresh():Promise<void> {
    if (!this.token()) return;
    try { const u=await firstValueFrom(this.api.get<Usuario>('auth/yo'));this.user.set(u);sessionStorage.setItem(USER,JSON.stringify(u));}
    catch { this.clear(); }
  }
  clear():void { sessionStorage.removeItem(TOKEN);sessionStorage.removeItem(USER);this.token.set(null);this.user.set(null); }
  async logout():Promise<void> {
    try { if(this.token()) await firstValueFrom(this.api.post<void>('auth/logout')); }
    catch {} finally {this.clear(); await this.router.navigateByUrl('/login');}
  }
}
