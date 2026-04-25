import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Bookmark } from '../../model/bookmark.model';

@Component({
  selector: 'app-bookmark-card',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatTooltipModule,
  ],
  templateUrl: './bookmark-card.component.html',
  styleUrl: './bookmark-card.component.scss',
})
export class BookmarkCardComponent {
  public readonly bookmark = input.required<Bookmark>();

  get displayTitle(): string {
    return this.bookmark().metadata?.title ?? this.bookmark().url;
  }

  get displayDomain(): string {
    return this.bookmark().metadata?.domain ?? new URL(this.bookmark().url).hostname;
  }

  get faviconUrl(): string | undefined {
    return this.bookmark().metadata?.favicon;
  }

  get isPending(): boolean {
    return this.bookmark().metadataStatus === 'PENDING';
  }

  get isFailed(): boolean {
    return this.bookmark().metadataStatus === 'FAILED';
  }

  public onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    
    img.style.display = 'none';
    img.parentElement!.classList.add('image-failed');
  }
}