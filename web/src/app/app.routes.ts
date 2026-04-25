import { Routes } from '@angular/router';
import { AppLayout } from './component/layout/layout.component';

export const routes: Routes = [
  { path: 'bookmarks', component: AppLayout },
  { path: '**', redirectTo: 'bookmarks' },
];