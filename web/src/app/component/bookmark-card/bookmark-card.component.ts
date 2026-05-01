import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Bookmark } from '../../model/bookmark.model';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { BookmarkApiService } from '../../service/bookmark-api.service';
import {
  BookmarkEditDialogComponent,
  BookmarkEditDialogResult
} from '../bookmark-edit-dialog/bookmark-edit-dialog.component';
import { BookmarkDeleteDialogComponent } from '../bookmark-delete-dialog/bookmark-delete-dialog.component';

@Component({
  selector: 'app-bookmark-card',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule,
  ],
  templateUrl: './bookmark-card.component.html',
  styleUrl: './bookmark-card.component.scss',
})
export class BookmarkCardComponent implements OnInit{
  public readonly bookmark = input.required<Bookmark>();

  public readonly isFavorite    = signal(false);
  public readonly notesExpanded = signal(false);

  private readonly dialog = inject(MatDialog);
  private readonly api    = inject(BookmarkApiService);

  public readonly updated = output<void>();
  public readonly deleted = output<void>();

  get displayTitle(): string {
    return this.bookmark().metadata?.title || this.bookmark().url;
  }

  get displayDomain(): string {
    return this.bookmark().metadata?.domain || new URL(this.bookmark().url).hostname;
  }

  get faviconUrl(): string | null {
    return this.bookmark().metadata?.favicon ?? null;
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

  public ngOnInit(): void {
    this.isFavorite.set(this.bookmark().favorite);
  }

  public toggleFavorite(): void {
    const status = !this.isFavorite();

    this.isFavorite.set(status); // ottimistico — aggiorna la UI subito

    this.api.updateBookmark(this.bookmark().id, { favorite: status })
      .subscribe({
        error: () => this.isFavorite.set(!status) // rollback se l'API fallisce
      });
  }

  public toggleNotes(): void {
    this.notesExpanded.set(!this.notesExpanded());
  }

  public openEditDialog(): void {
    const ref = this.dialog.open(BookmarkEditDialogComponent, {
      data: { bookmark: this.bookmark() },
      width: '440px',
    });

    ref.afterClosed().subscribe((result: BookmarkEditDialogResult | undefined) => {
      if (!result) return;

      this.api.updateBookmark(this.bookmark().id, {
        notes:   result.notes,
        listIds: result.listIds.map(id => id) as number[],
        tagIds:  result.tagIds.map(id  => id) as number[],
      }).subscribe(() => this.updated.emit());
    });
  }

  public openDeleteDialog(): void {
    const ref = this.dialog.open(BookmarkDeleteDialogComponent, {
      data: {
        title: this.bookmark().metadata?.title,
        url: this.bookmark().url,
      },
      width: '440px',
    });

    ref.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      
      this.api.deleteBookmark(this.bookmark().id)
        .subscribe(() => this.deleted.emit());
    });
  }
}