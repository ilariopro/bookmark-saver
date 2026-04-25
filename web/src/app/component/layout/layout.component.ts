import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { MediaMatcher } from '@angular/cdk/layout';
import { CommonModule } from '@angular/common';

import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { AppSidebar } from '../sidebar/sidebar.component';
import { BookmarkApiService } from '../../service/bookmark-api.service';
import { FilterStateService } from '../../service/filter-state.service';
import { AppBookmarks } from "../bookmarks/bookmarks.component";

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    AppBookmarks,
    AppSidebar,
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatSidenavModule,
    MatToolbarModule,
],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss',
})
export class AppLayout implements OnInit, OnDestroy {
  private readonly api   = inject(BookmarkApiService);
  readonly state         = inject(FilterStateService);

  readonly isMobile      = signal(false);
  readonly sidebarLoading = signal(false);
  readonly sidebarError   = signal<string | null>(null);

  private readonly _mql: MediaQueryList;
  private readonly _mqlListener: () => void;

  constructor() {
    const media = inject(MediaMatcher);
    this._mql = media.matchMedia('(max-width: 768px)');
    this.isMobile.set(this._mql.matches);
    this._mqlListener = () => this.isMobile.set(this._mql.matches);
    this._mql.addEventListener('change', this._mqlListener);
  }

  ngOnInit(): void {
    this.loadSidebarData();
  }

  ngOnDestroy(): void {
    this._mql.removeEventListener('change', this._mqlListener);
  }

  private loadSidebarData(): void {
    this.sidebarLoading.set(true);
    this.sidebarError.set(null);

    this.api.getLists().subscribe({
      next: lists => {
        this.state.apiLists.set(lists);

        if (!this.state.hasSelectedList()) {
          this.state.selectList('all');
        }
      },
      error: () => this.sidebarError.set('Error loading lists.'),
    });

    this.api.getTags().subscribe({
      next: tags => {
        this.state.tags.set(tags);
        this.sidebarLoading.set(false);
      },
      error: () => {
        this.sidebarError.set('Error loading tags.');
        this.sidebarLoading.set(false);
      },
    });
  }
}