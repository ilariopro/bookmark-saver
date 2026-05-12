import {
  Component, inject, effect, viewChild,
  ElementRef, AfterViewInit, OnDestroy,
  Injector,
  computed,
} from '@angular/core';
import { CommonModule, DOCUMENT } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';

import { BookmarkApiService } from '../../service/bookmark-api.service';
import { FilterStateService } from '../../service/filter-state.service';
import { ResponsiveStateService } from '../../service/responsive-state.service';
import { InfiniteScrollService } from '../../service/infinite-scroll.service';
import { BookmarkCardComponent } from '../bookmark-card/bookmark-card.component';
import { Bookmark, BookmarkQueryParams } from '../../model/bookmark.model';
import { firstValueFrom } from 'rxjs';
import { MetadataPollingService } from '../../service/metadata-polling.servie';
import { BookmarkFormDialogComponent, BookmarkFormDialogResult } from '../bookmark-form-dialog/bookmark-form-dialog.component';
import { NotificationService } from '../../service/notification.service';
import { MatSidenavContent } from '@angular/material/sidenav';

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
  private readonly api            = inject(BookmarkApiService);
  private readonly dialog         = inject(MatDialog);
  private readonly injector       = inject(Injector);
  private readonly metadata       = inject(MetadataPollingService);
  private readonly notify         = inject(NotificationService);
  public  readonly responsive     = inject(ResponsiveStateService);
  public  readonly scroll         = inject(InfiniteScrollService<Bookmark>);
  public  readonly state          = inject(FilterStateService);
  
  public  readonly showBackToTop = computed(() => this.scroll.total() >= 24);
  
  private readonly sentinel = viewChild<ElementRef>('sentinel');
  private readonly topRef   = viewChild<ElementRef>('top');

  constructor() {
    this.scroll.setLoader(page => {
      const params = this.extractQueryParams();

      const bookmarks = this.api.getBookmarks(
        params.favorite,
        params.archived,
        params.untagged,
        params.listId,
        params.tagIds,
        page
      );

      return firstValueFrom(bookmarks);
    });

    effect(
      () => {
        const list = this.state.selectedList();

        if (!list) return;

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
    const ref = this.dialog.open(BookmarkFormDialogComponent, { data: {}, width: '440px' });

    ref.afterClosed().subscribe((result: BookmarkFormDialogResult | undefined) => {
      if (!result) return;

      this.api.createBookmark({
        url:     result.url!,
        notes:   result.notes,
        listIds: result.listIds,
        tagIds:  result.tagIds
      }).subscribe(bookmark => {
        this.scroll.reset();
        this.notify.success('Bookmark created');

        if (bookmark.metadataStatus === 'PENDING') {
          this.metadata.pollUntilResolved(bookmark.id, resolved => {
            this.scroll.items.update(prev =>
              prev.map(b => b.id === resolved.id ? resolved : b)
            );
          });
        }
      });
    });
  }

  public scrollToTop(): void {
    this.topRef()?.nativeElement.scrollIntoView({ behavior: 'smooth' });
  }

  private extractQueryParams(): BookmarkQueryParams {
    const list = this.state.selectedList();

    let archived: boolean | null = null; 
    
    if (list?.type === 'default' && list.id === 'archived') {
      archived = true;
    }

    if (list?.type === 'default' && list.id === 'bookmarks') {
      archived = false;
    }

    return {
      favorite: list?.type === 'default' && list.id === 'favorites',
      archived,
      untagged: list?.type === 'default' && list.id === 'untagged',
      listId:   list?.type === 'api' ? list.id : null,
      tagIds:   this.state.selectedTagIdsArray(),
    };
  }

  private refresh(): void {
    const params = this.extractQueryParams();

    const bookmarks = this.api.getBookmarks(
        params.favorite,
        params.archived,
        params.untagged,
        params.listId,
        params.tagIds
      );

    firstValueFrom(bookmarks)
      .then(response => {
        this.scroll.items.set(response.data);
        this.scroll.total.set(response.meta.total);
      });
  }
}