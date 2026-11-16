import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './core/guards';
import { ShellComponent } from './layout/shell';
export const routes:Routes=[
 {path:'login',loadComponent:()=>import('./pages/login').then(m=>m.LoginComponent)},
 {path:'app',component:ShellComponent,canActivate:[authGuard],children:[
  {path:'dashboard',loadComponent:()=>import('./pages/dashboard').then(m=>m.DashboardComponent)},
  {path:'alertas',loadComponent:()=>import('./pages/alertas').then(m=>m.AlertasComponent)},
  {path:'calendario',loadComponent:()=>import('./pages/calendario').then(m=>m.CalendarioComponent)},
  {path:'reportes',canActivate:[adminGuard],loadComponent:()=>import('./pages/reportes').then(m=>m.ReportesComponent)},
  {path:'actividades',loadComponent:()=>import('./pages/actividades').then(m=>m.ActividadesComponent)},
  {path:'turnos',loadComponent:()=>import('./pages/turnos').then(m=>m.TurnosComponent)},
  {path:'pacientes',loadComponent:()=>import('./pages/pacientes').then(m=>m.PacientesComponent)},
  {path:'personal',canActivate:[adminGuard],loadComponent:()=>import('./pages/personal').then(m=>m.PersonalComponent)},
  {path:'novedades',loadComponent:()=>import('./pages/novedades').then(m=>m.NovedadesComponent)},
  {path:'auditoria',canActivate:[adminGuard],loadComponent:()=>import('./pages/auditoria').then(m=>m.AuditoriaComponent)},
  {path:'cuenta',loadComponent:()=>import('./pages/cuenta').then(m=>m.CuentaComponent)},
  {path:'',pathMatch:'full',redirectTo:'dashboard'}]},
 {path:'',pathMatch:'full',redirectTo:'app/dashboard'},
 {path:'**',redirectTo:'app/dashboard'}
];
