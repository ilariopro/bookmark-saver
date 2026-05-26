import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckbox } from '@angular/material/checkbox';
import { MatChipListboxChange, MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Bookmark } from '../../model/bookmark.model';
import { BookmarkApiService } from '../../service/bookmark-api.service';
import { BookmarkDeleteDialogComponent, BookmarkDeleteDialogData } from '../bookmark-delete-dialog/bookmark-delete-dialog.component';
import { BookmarkEditDialogComponent, BookmarkEditDialogResult } from '../bookmark-edit-dialog/bookmark-edit-dialog.component';
import { NotificationService } from '../../service/notification.service';
import { FilterStateService } from '../../service/filter-state.service';

@Component({
  selector: 'bookmark-card',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatCheckbox,
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
  private readonly api    = inject(BookmarkApiService);
  private readonly dialog = inject(MatDialog);
  private readonly notify = inject(NotificationService);
  private readonly state  = inject(FilterStateService);
  
  public readonly isFavorite    = signal(false);
  public readonly notesExpanded = signal(false);
  
  public readonly bookmark       = input.required<Bookmark>();
  public readonly selectable     = input(false);
  public readonly selected       = input(false);
  public readonly selectedChange = output<boolean>();

  public readonly selectedTagIds = computed(() =>
    this.state.selectedTagIdsArray()
  );
  
  public readonly updated = output<void>();
  public readonly deleted = output<void>();

  get displayTitle(): string {
    return this.bookmark().metadata?.title || this.bookmark().url;
  }

  get displayDomain(): string {
    return this.bookmark().metadata?.domain || new URL(this.bookmark().url).hostname;
  }

  get displayTags() {
    return [...this.bookmark().tags].sort((a, b) => a.name.localeCompare(b.name));
  }

  get createdAt(): string {
    return new Date(this.bookmark().createdAt).toLocaleString();
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

  public toggleArchive(): void {
    const archived = !this.bookmark().archived;

    this.api.updateBookmark(this.bookmark().id, { archived }).subscribe({
      next: () => {
        this.notify.success(archived ? 'Bookmark archived' : 'Bookmark unarchived');
        this.updated.emit();
      },
      error: () => this.notify.error('Could not update bookmark.'),
    });
  }

  public toggleNotes(): void {
    this.notesExpanded.set(!this.notesExpanded());
  }

  public toggleSelect(event: MouseEvent): void {
    if (!this.selectable()) return;

    event.stopPropagation();

    this.selectedChange.emit(!this.selected());
  }

  public onTagsChange(event: MatChipListboxChange): void {
    this.state.setSelectedTags(event.value ?? []);
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
        tagIds:  result.tagIds,
      }).subscribe(() => {
        this.updated.emit();
        this.notify.success('Bookmark updated');
      });
    });
  }

  public openDeleteDialog(): void {
    const ref = this.dialog.open(BookmarkDeleteDialogComponent, {
      data: {
        url:         this.bookmark().url,
        description: 'Are you sure you want to delete this bookmark?',
      } satisfies BookmarkDeleteDialogData,
      width: '440px',
    });

    ref.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      
      this.api.deleteBookmark(this.bookmark().id)
        .subscribe(() => {
          this.deleted.emit();
          this.notify.success('Bookmark deleted');
        });
    });
  }
}