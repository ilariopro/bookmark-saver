import {
  Component, inject, effect, viewChild,
  ElementRef, AfterViewInit, OnDestroy,
  Injector,
  computed,
  signal,
} from '@angular/core';
import { CommonModule, DOCUMENT } from '@angular/common';
import { firstValueFrom } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSidenavContent } from '@angular/material/sidenav';

import { BookmarkApiService } from '../../service/bookmark-api.service';
import { FilterStateService } from '../../service/filter-state.service';
import { ResponsiveStateService } from '../../service/responsive-state.service';
import { InfiniteScrollService } from '../../service/infinite-scroll.service';
import { BookmarkCardComponent } from '../bookmark-card/bookmark-card.component';
import { Bookmark, BookmarkQueryParams } from '../../model/bookmark.model';
import { MetadataPollingService } from '../../service/metadata-polling.servie';
import { BookmarkEditDialogComponent, BookmarkEditDialogResult } from '../bookmark-edit-dialog/bookmark-edit-dialog.component';
import { NotificationService } from '../../service/notification.service';
import { BulkActionBarComponent } from '../bulk-action-bar/bulk-action-bar.component';

@Component({
  selector: 'bookmark-list',
  standalone: true,
  providers: [InfiniteScrollService],
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    BookmarkCardComponent,
    BulkActionBarComponent
  ],
  templateUrl: './bookmark-list.component.html',
  styleUrl:    './bookmark-list.component.scss',
})
export class BookmarkList implements AfterViewInit, OnDestroy {
  private readonly api            = inject(BookmarkApiService);
  private readonly dialog         = inject(MatDialog);
  private readonly document       = inject(DOCUMENT);
  private readonly injector       = inject(Injector);
  private readonly metadata       = inject(MetadataPollingService);
  private readonly notify         = inject(NotificationService);
  public  readonly responsive     = inject(ResponsiveStateService);
  public  readonly scroll         = inject(InfiniteScrollService<Bookmark>);
  private readonly sidenavContent = inject(MatSidenavContent, { optional: true });
  public  readonly state          = inject(FilterStateService);

  private readonly sentinel = viewChild<ElementRef>('sentinel');

  public readonly editMode    = signal(false);
  public readonly selectedIds = signal<Set<number>>(new Set());
  
  public readonly selectedIdsArray = computed(() => Array.from(this.selectedIds()));

  constructor() {
    this.scroll.setLoader(page => {
      const params = this.extractQueryParams();

      const bookmarks = this.api.getBookmarks(
        params.favorite,
        params.archived,
        params.untagged,
        params.tagId,
        page
      );

      return firstValueFrom(bookmarks);
    });

    effect(
      () => {
        const list = this.state.selectedList();

        if (!list) return;

        this.state.selectedTagIdsArray();
        this.selectedIds.set(new Set());
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
    const ref = this.dialog.open(BookmarkEditDialogComponent, { data: {}, width: '440px' });

    ref.afterClosed().subscribe((result: BookmarkEditDialogResult | undefined) => {
      if (!result) return;

      this.api.createBookmark({
        url:     result.url!,
        notes:   result.notes,
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

  public toggleEditMode(): void {
    this.editMode.update(editMode => !editMode);
    this.selectedIds.set(new Set());
  }

  public toggleCardSelection(id: number, selected: boolean): void {
    this.selectedIds.update(prev => {
      const selection = new Set(prev);

      if (selected) {
        selection.add(id);
      } else {
        selection.delete(id);
      }

      return selection;
    });
  }

  public onBulkDone(): void {
    this.editMode.set(false);
    this.selectedIds.set(new Set());
    this.reload();
  }

  public scrollToTop(): void {    
    if (this.responsive.isMobile()) {
      this.document.defaultView?.scrollTo(0, 0);
    } else {
      this.sidenavContent?.scrollTo({ top: 0, behavior: 'instant' });
    }
  }

  private extractQueryParams(): BookmarkQueryParams {
    const list = this.state.selectedList();

    let archived: boolean | null = null; 
    
    if (list?.type === 'default' && list.id === 'archived')  archived = true;
    if (list?.type === 'default' && list.id === 'bookmarks') archived = false;

    return {
      favorite: list?.type === 'default' && list.id === 'favorites',
      archived,
      untagged: list?.type === 'default' && list.id === 'untagged',
      tagId:    list?.type === 'tag' ? list.id : null,
    };
  }

  private refresh(): void {
    const params = this.extractQueryParams();

    const bookmarks = this.api.getBookmarks(
        params.favorite,
        params.archived,
        params.untagged,
        params.tagId
      );

    firstValueFrom(bookmarks)
      .then(response => {
        this.scroll.items.set(response.data);
        this.scroll.total.set(response.meta.total);
      });
  }
}