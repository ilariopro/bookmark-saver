import { Component, OnInit, inject, signal } from '@angular/core';
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
import { ResponsiveStateService } from '../../service/responsive-state.service';

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
export class AppLayout implements OnInit {
  private readonly api        = inject(BookmarkApiService);
  private readonly responsive = inject(ResponsiveStateService);
  private readonly state      = inject(FilterStateService);

  public readonly sidebarLoading = signal(false);
  public readonly sidebarError   = signal<string | null>(null);

  public ngOnInit(): void {
    this.loadSidebarData();
  }

  public isMobile(): boolean {
    return this.responsive.isMobile();
  }

  private loadSidebarData(): void {
    this.sidebarLoading.set(true);
    this.sidebarError.set(null);

    this.api.getLists().subscribe({
      next: lists => {
        this.state.apiLists.set(lists);
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