import { Routes } from '@angular/router';
import { AppLayout } from './component/layout/layout.component';

export const routes: Routes = [
  { path: 'bookmarks', component: AppLayout },
  { path: 'favorites', component: AppLayout },
  { path: 'archived',  component: AppLayout },
  { path: 'lists/:id', component: AppLayout },
  { path: '',          redirectTo: 'bookmarks', pathMatch: 'full' },
  { path: '**',        redirectTo: 'bookmarks' },
];