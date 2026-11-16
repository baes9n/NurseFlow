import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet, NavigationEnd } from '@angular/router';
import { filter, firstValueFrom } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { ToastService } from '../core/toast.service';
import { IconComponent } from '../shared/icon';

@Component({selector:'nf-shell',standalone:true,imports:[RouterOutlet,RouterLink,RouterLinkActive,IconComponent],template:`
<div class="app-shell">
  <div class="mobile-overlay" [class.show]="menuOpen()" (click)="menuOpen.set(false)"></div>
  <aside class="sidebar" [class.is-open]="menuOpen()">
    <a class="brand" routerLink="/app/dashboard" (click)="menuOpen.set(false)">
      <span class="brand-mark"><span>✚</span></span><span class="brand-copy">NurseFlow<small>GESTIÓN CLÍNICA</small></span>
    </a>
    <div class="workspace-label">ESPACIO DE TRABAJO</div>
    <div class="nav-block">
      <a class="nav-item" routerLink="/app/dashboard" routerLinkActive="active"><nf-icon name="dashboard"/> <span>Resumen general</span></a>
      <a class="nav-item" routerLink="/app/alertas" routerLinkActive="active"><nf-icon name="bell"/><span>Alertas operativas</span></a>
      <a class="nav-item" routerLink="/app/calendario" routerLinkActive="active"><nf-icon name="calendar"/><span>Calendario</span></a>
      <a class="nav-item" routerLink="/app/actividades" routerLinkActive="active"><nf-icon name="tasks"/><span>Actividades</span></a>
      <a class="nav-item" routerLink="/app/turnos" routerLinkActive="active"><nf-icon name="calendar"/><span>Turnos</span></a>
      <a class="nav-item" routerLink="/app/pacientes" routerLinkActive="active"><nf-icon name="patient"/><span>Pacientes</span></a>
      @if(auth.isAdmin()){
        <a class="nav-item" routerLink="/app/personal" routerLinkActive="active"><nf-icon name="users"/><span>Personal</span></a>
      }
      <a class="nav-item" routerLink="/app/novedades" routerLinkActive="active"><nf-icon name="bell"/><span>Novedades</span></a>
    </div>
    @if(auth.isAdmin()){
      <div class="workspace-label spaced">CONTROL Y SEGURIDAD</div>
      <div class="nav-block"><a class="nav-item" routerLink="/app/reportes" routerLinkActive="active"><nf-icon name="report"/><span>Reportes PDF</span></a>
      <a class="nav-item" routerLink="/app/auditoria" routerLinkActive="active"><nf-icon name="shield"/><span>Auditoría</span></a></div>
    }
    <div class="sidebar-spacer"></div>
    <div class="sidebar-foot">
      <a class="nav-item" routerLink="/app/cuenta" routerLinkActive="active"><nf-icon name="settings"/><span>Mi cuenta</span></a>
      <div class="sidebar-user"><div class="avatar dark">{{initials()}}</div><div class="sidebar-user-copy"><strong>{{auth.user()?.correo}}</strong><small>{{auth.isAdmin()?'Administrador':'Enfermería'}}</small></div></div>
    </div>
  </aside>
  <div class="main-column">
    <header class="topbar">
      <div class="topbar-left"><button type="button" class="icon-btn mobile-menu" aria-label="Abrir menú" (click)="menuOpen.set(!menuOpen())"><nf-icon name="menu"/></button><span class="breadcrumb">NurseFlow <span>/</span> <strong>{{pageTitle()}}</strong></span></div>
      <div class="topbar-right"><span class="topbar-env"><span class="live-dot" [class.offline]="health()===false"></span> {{health()===true?'API conectada':health()===false?'Sin conexión con la API':'Verificando conexión'}}</span><span class="topbar-sep"></span><div class="avatar light">{{initials()}}</div><button type="button" class="icon-btn logout-btn" (click)="auth.logout()" title="Cerrar sesión" aria-label="Cerrar sesión"><nf-icon name="logout"/></button></div>
    </header>
    <main class="page-container"><router-outlet/></main>
  </div>
</div>
<div class="toast-stack" aria-live="polite">@for(t of toast.items();track t.id){<div class="toast" [class]="'toast '+t.type"><span class="toast-icon">{{t.type==='success'?'✓':t.type==='error'?'!':'i'}}</span><span>{{t.message}}</span><button type="button" aria-label="Cerrar aviso" (click)="toast.dismiss(t.id)">×</button></div>}</div>
`})
export class ShellComponent implements OnInit {
 auth=inject(AuthService);toast=inject(ToastService);private router=inject(Router);private api=inject(ApiService);health=signal<boolean|null>(null);
 menuOpen=signal(false);pageTitle=signal('Resumen general');
 private titles:Record<string,string>={dashboard:'Resumen general',alertas:'Alertas operativas',calendario:'Calendario',reportes:'Reportes PDF',actividades:'Actividades',turnos:'Turnos',pacientes:'Pacientes',personal:'Personal',novedades:'Novedades',auditoria:'Auditoría',cuenta:'Mi cuenta'};
 ngOnInit(){void this.auth.refresh();void this.checkHealth();this.updatePage();this.router.events.pipe(filter(e=>e instanceof NavigationEnd)).subscribe(()=>{this.menuOpen.set(false);this.updatePage();});}
 private async checkHealth(){try{await firstValueFrom(this.api.get('sistema/estado'));this.health.set(true);}catch{this.health.set(false);}}
 private updatePage(){const part=this.router.url.split('?')[0].split('/').pop()||'dashboard';this.pageTitle.set(this.titles[part]||'NurseFlow');}
 initials(){return (this.auth.user()?.correo||'NF').slice(0,2).toUpperCase();}
}
