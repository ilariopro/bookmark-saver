import {
  Component, inject, effect, viewChild,
  ElementRef, AfterViewInit, OnDestroy,
  Injector
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';

import { BookmarkApiService } from '../../service/bookmark-api.service';
import { FilterStateService } from '../../service/filter-state.service';
import { ResponsiveStateService } from '../../service/responsive-state.service';
import { InfiniteScrollService } from '../../service/infinite-scroll.service';
import { BookmarkCardComponent } from '../bookmark-card/bookmark-card.component';
import { BookmarkAddDialogComponent, BookmarkAddDialogResult } from '../bookmark-add-dialog/bookmark-add-dialog.component';
import { Bookmark, BookmarkQueryParams } from '../../model/bookmark.model';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-bookmarks',
  standalone: true,
  providers: [InfiniteScrollService],
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    BookmarkCardComponent,
  ],
  templateUrl: './bookmarks.component.html',
  styleUrl: './bookmarks.component.scss',
})
export class AppBookmarks implements AfterViewInit, OnDestroy {
  private readonly api        = inject(BookmarkApiService);
  private readonly dialog     = inject(MatDialog);
  public  readonly state      = inject(FilterStateService);
  public  readonly responsive = inject(ResponsiveStateService);
  public  readonly scroll     = inject(InfiniteScrollService<Bookmark>);

  private readonly injector = inject(Injector);
  private readonly sentinel = viewChild<ElementRef>('sentinel');
  private readonly topRef   = viewChild<ElementRef>('top');

  constructor() {
    this.scroll.setLoader(page => {
      const { favorite, listId, tagIds } = this.extractQueryParams();

      return firstValueFrom(this.api.getBookmarks(favorite, listId, tagIds, page));
    });

    effect(
      () => {
        this.state.selectedList();
        this.state.selectedTagIdsArray();
        this.reload();
      },
      { allowSignalWrites: true }
    );

    effect(
      () => {
        this.state.tags();
        this.refresh();
      },
      { allowSignalWrites: true }
    );
  }

  public ngAfterViewInit(): void {
    effect(
      () => {
        const element= this.sentinel();

        if (element) this.scroll.observeSentinel(element);
      },
      { injector: this.injector }
    );
  }

  public ngOnDestroy(): void {
    this.scroll.disconnect();
  }

  public reload(): void {
    this.scroll.reset();
  }

  public hasBookmarks(): boolean {
    return !this.scroll.loading() && this.scroll.total() > 0;
  }

  public openAddDialog(): void {
    const ref = this.dialog.open(BookmarkAddDialogComponent, { width: '440px' });

    ref.afterClosed().subscribe((result: BookmarkAddDialogResult | undefined) => {
      if (!result) return;

      this.api.createBookmark({
        url:     result.url,
        notes:   result.notes,
        listIds: result.listIds.map(id => ({ id })) as any,
        tagIds:  result.tagIds.map(id  => ({ id })) as any,
      }).subscribe(() => this.scroll.reset());
    });
  }

  public scrollToTop(): void {
    this.topRef()?.nativeElement.scrollIntoView({ behavior: 'instant' });
  }

  private extractQueryParams(): BookmarkQueryParams {
    const list = this.state.selectedList();

    return {
      favorite: list?.type === 'default' && list?.id === 'favorites',
      listId:   list?.type === 'api' ? Number(list.id) : null,
      tagIds:   this.state.selectedTagIdsArray(),
    };
  }

  private refresh(): void {
    const { favorite, listId, tagIds } = this.extractQueryParams();

    firstValueFrom(this.api.getBookmarks(favorite, listId, tagIds, 0))
      .then(response => {
        this.scroll.items.set(response.data);
        this.scroll.total.set(response.meta.total);
      });
  }
}