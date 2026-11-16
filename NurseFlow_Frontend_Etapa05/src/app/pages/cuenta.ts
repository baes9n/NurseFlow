import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { ApiService } from '../core/api.service';
import { ToastService } from '../core/toast.service';
import { apiMessage } from '../core/errors';
import { IconComponent } from '../shared/icon';
@Component({selector:'nf-cuenta',standalone:true,imports:[FormsModule,IconComponent],template:`
<div class="page-head"><div><div class="eyebrow">PREFERENCIAS</div><h1>Mi cuenta</h1><p>Consulta tu perfil y administra las credenciales de acceso.</p></div></div>
<div class="settings-grid"><section class="surface profile-panel"><div class="panel-heading"><div><h2>Información de cuenta</h2><p>Perfil vinculado a NurseFlow</p></div><nf-icon name="user"/></div><div class="profile-avatar">{{initials()}}</div><h3>{{auth.user()?.correo}}</h3><span class="badge teal">{{auth.isAdmin()?'Administrador':'Personal de enfermería'}}</span><div class="profile-detail"><span>Identificador</span><strong>#{{auth.user()?.id}}</strong></div><div class="profile-detail"><span>Estado</span><strong>{{auth.user()?.activo?'Activo':'Inactivo'}}</strong></div></section>
<section class="surface profile-panel"><div class="panel-heading"><div><h2>Actualizar contraseña</h2><p>Al cambiarla, se revocarán las sesiones existentes.</p></div><nf-icon name="lock"/></div><form (ngSubmit)="save()" #f="ngForm"><label class="field"><span>Contraseña actual *</span><input type="password" name="actual" autocomplete="current-password" [(ngModel)]="actual" required/></label><label class="field"><span>Nueva contraseña *</span><input type="password" name="nueva" autocomplete="new-password" [(ngModel)]="nueva" required minlength="12" maxlength="100"/><small>Mínimo 12 caracteres.</small></label><label class="field"><span>Confirmar contraseña *</span><input type="password" name="confirmacion" autocomplete="new-password" [(ngModel)]="confirmacion" required/></label>@if(error()){<div class="alert error"><nf-icon name="alert"/>{{error()}}</div>}<button class="btn btn-primary" type="submit" [disabled]="f.invalid||saving()">{{saving()?'Actualizando…':'Cambiar contraseña'}}</button></form></section></div>
`})
export class CuentaComponent {
 auth=inject(AuthService);private api=inject(ApiService);private toast=inject(ToastService);
 actual='';nueva='';confirmacion='';error=signal('');saving=signal(false);
 initials(){return (this.auth.user()?.correo||'NF').slice(0,2).toUpperCase();}
 async save(){this.error.set('');if(this.nueva!==this.confirmacion){this.error.set('Las contraseñas no coinciden.');return;}if(this.nueva.length<12){this.error.set('La contraseña debe tener al menos 12 caracteres.');return;}if(this.saving())return;
 this.saving.set(true);try{await firstValueFrom(this.api.patch('auth/contrasena',{actual:this.actual,nueva:this.nueva}));this.toast.show('Contraseña modificada. Inicia sesión de nuevo.');this.actual='';this.nueva='';this.confirmacion='';this.auth.clear();await this.auth.logout();}catch(e){this.error.set(apiMessage(e));}finally{this.saving.set(false);}}
}
