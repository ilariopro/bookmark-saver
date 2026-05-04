import { Injectable, inject } from '@angular/core';
import { interval, Subscription, firstValueFrom } from 'rxjs';
import { BookmarkApiService } from './bookmark-api.service';
import { Bookmark } from '../model/bookmark.model';

@Injectable({ providedIn: 'root' })
export class MetadataPollingService {
  private readonly api = inject(BookmarkApiService);
  private readonly POLL_INTERVAL_MS = 3000;
  private readonly MAX_ATTEMPTS = 10;

  pollUntilResolved(
    bookmarkId: number,
    onResolved: (bookmark: Bookmark) => void
  ): void {
    let attempts = 0;

    const subscription: Subscription = interval(this.POLL_INTERVAL_MS).subscribe(async () => {
      attempts++;

      const bookmark = await firstValueFrom(this.api.getBookmark(bookmarkId));

      if (bookmark.metadataStatus !== 'PENDING' || attempts >= this.MAX_ATTEMPTS) {
        subscription.unsubscribe();
        
        onResolved(bookmark);
      }
    });
  }
}