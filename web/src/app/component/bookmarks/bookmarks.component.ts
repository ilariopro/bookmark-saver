import { Component, inject, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { BookmarkApiService } from '../../service/bookmark-api.service';
import { FilterStateService } from '../../service/filter-state.service';
import { BookmarkCardComponent } from '../bookmark-card/bookmark-card.component';
import { Bookmark } from '../../model/bookmark.model';

@Component({
  selector: 'app-bookmarks',
  standalone: true,
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    BookmarkCardComponent,
  ],
  templateUrl: './bookmarks.component.html',
  styleUrl: './bookmarks.component.scss',
})
export class AppBookmarks {
  private readonly api  = inject(BookmarkApiService);
  public readonly state = inject(FilterStateService);

  public readonly bookmarks = signal<Bookmark[]>([]);
  public readonly loading   = signal(false);
  public readonly error     = signal<string | null>(null);

  constructor() {
    effect(
      () => {
        const list   = this.state.selectedList();
        const tagIds = this.state.selectedTagIdsArray();

        if (!list) return;

        const listId   = list.type === 'api' ? Number(list.id) : null;

        this.load(listId, tagIds);
      },
      { allowSignalWrites: true }
    );
  }

  private load(listId: number | null, tagIds: number[]): void {
    this.loading.set(true);
    this.error.set(null);

    this.api.getBookmarks(listId, tagIds).subscribe({
      next: response => {
        this.bookmarks.set(response.data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load bookmarks.');
        this.loading.set(false);
      },
    });
  }
}