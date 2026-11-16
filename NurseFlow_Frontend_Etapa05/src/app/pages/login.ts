import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { apiMessage } from '../core/errors';
import { IconComponent } from '../shared/icon';
@Component({selector:'nf-login',standalone:true,imports:[FormsModule,IconComponent],template:`
<div class="login-screen">
  <div class="login-left">
    <a class="brand login-brand"><span class="brand-mark"><span>✚</span></span><span class="brand-copy">NurseFlow<small>GESTIÓN CLÍNICA</small></span></a>
    <div class="login-showcase"><div class="eyebrow inverted"><span class="live-dot"></span> PLATAFORMA DE ENFERMERÍA</div><h1>Una atención mejor<br>empieza con una<br><em>gestión más clara.</em></h1><p>Organiza turnos, coordina actividades y mantén cada procedimiento bajo control, desde un solo lugar.</p>
      <div class="showcase-card"><div class="showcase-icon"><nf-icon name="activity"/></div><div><strong>Todo el equipo sincronizado</strong><span>Información actualizada desde el sistema</span></div><span class="showcase-check">✓</span></div>
    </div>
    <span class="login-left-foot">NurseFlow · Proyecto académico</span>
  </div>
  <div class="login-right"><div class="login-panel">
    <div class="login-icon"><nf-icon name="lock"/></div>
    <div class="eyebrow">BIENVENIDO DE NUEVO</div><h2>Inicia sesión</h2><p>Ingresa tus credenciales para acceder al sistema.</p>
    @if(error()){<div class="alert error" role="alert"><nf-icon name="alert"/>{{error()}}</div>}
    <form (ngSubmit)="submit()" #form="ngForm">
      <label class="field"><span>Correo electrónico</span><input name="correo" type="email" placeholder="usuario@institucion.com" [(ngModel)]="correo" required email autocomplete="username"/></label>
      <label class="field"><span>Contraseña</span><div class="password-wrap"><input name="contrasena" [type]="showPass()?'text':'password'" placeholder="Ingresa tu contraseña" [(ngModel)]="contrasena" required autocomplete="current-password"/><button type="button" class="password-eye" (click)="showPass.set(!showPass())">{{showPass()?'Ocultar':'Mostrar'}}</button></div></label>
      <button class="btn btn-primary login-submit" type="submit" [disabled]="form.invalid||loading()">{{loading()?'Ingresando…':'Ingresar al sistema'}}<nf-icon name="arrow"/></button>
    </form>
    <div class="login-notice"><nf-icon name="shield"/><span>Acceso exclusivo para personal autorizado. Las acciones importantes quedan auditadas.</span></div>
  </div><div class="login-right-foot">© NurseFlow · Entorno de demostración académica</div></div>
</div>`})
export class LoginComponent {
 private auth=inject(AuthService);
 correo='';contrasena='';loading=signal(false);error=signal('');showPass=signal(false);
 async submit(){if(!this.correo||!this.contrasena||this.loading())return;this.error.set('');this.loading.set(true);
  try{await this.auth.login(this.correo,this.contrasena);}catch(e){this.error.set(apiMessage(e));}finally{this.loading.set(false);}}
}
