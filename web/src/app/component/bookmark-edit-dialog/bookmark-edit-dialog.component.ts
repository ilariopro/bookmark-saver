import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipListboxChange, MatChipsModule } from '@angular/material/chips';
import { MatIcon } from '@angular/material/icon';

import { Bookmark } from '../../model/bookmark.model';
import { FilterStateService } from '../../service/filter-state.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { BookmarkApiService } from '../../service/bookmark-api.service';
import { ListFormDialogComponent, ListFormDialogResult } from '../list-form-dialog/list-form-dialog.component';
import { TagFormDialogComponent, TagFormDialogResult } from '../tag-form-dialog/tag-form-dialog.component';

export interface BookmarkEditDialogData {
  bookmark: Bookmark;
}

export interface BookmarkEditDialogResult {
  notes: string;
  listIds: number[];
  tagIds: number[];
}

@Component({
  selector: 'app-bookmark-edit',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatChipsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIcon,
    MatInputModule,
  ],
  templateUrl: './bookmark-edit-dialog.component.html',
  styleUrl: './bookmark-edit-dialog.component.scss',
})
export class BookmarkEditDialogComponent {
  private readonly api    = inject(BookmarkApiService);
  private readonly dialog = inject(MatDialog);
  private readonly ref    = inject(MatDialogRef<BookmarkEditDialogComponent>);

  public readonly data: BookmarkEditDialogData = inject(MAT_DIALOG_DATA);
  public readonly state = inject(FilterStateService);

  public notes = this.data.bookmark.notes ?? '';

  readonly selectedTagIds = signal<number[]>(this.data.bookmark.tags.map(t => t.id));
  readonly selectedListIds = signal<number[]>(this.data.bookmark.lists.map(l => l.id));

  public onListsChange(event: MatChipListboxChange): void {
    this.selectedListIds.set(event.value ?? []);
  }

  public onTagsChange(event: MatChipListboxChange): void {
    this.selectedTagIds.set(event.value ?? []);
  }

  public isListSelected(id: number): boolean {
    return this.selectedListIds().includes(id);
  }

  public openCreateListDialog(): void {
    const ref = this.dialog.open(ListFormDialogComponent, {
      data: {},
      width: '440px',
    });

    ref.afterClosed().subscribe((result: ListFormDialogResult | undefined) => {
      if (!result) return;

      this.api.createList({ name: result.name, description: result.description })
        .subscribe(list => {
          this.selectedListIds.update(prev => [...prev, list.id]);
          this.api.getLists().subscribe(lists => this.state.apiLists.set(lists));
        });
    });
  }

  public openCreateTagDialog(): void {
    const ref = this.dialog.open(TagFormDialogComponent, {
      data: {},
      width: '440px',
    });

    ref.afterClosed().subscribe((result: TagFormDialogResult | undefined) => {
      if (!result) return;

      this.api.createTag({ name: result.name }).subscribe(tag => {
        this.selectedTagIds.update(prev => [...prev, tag.id]);
        this.api.getTags().subscribe(tags => this.state.tags.set(tags));
      });
    });
  }

  public toggleList(id: number, checked: boolean): void {
    if (checked) {
      this.selectedListIds.update(prev => [...prev, id]);
    } else {
      this.selectedListIds.update(prev => prev.filter(listId => listId !== id));
    }
  }

  public save(): void {
    this.ref.close({
      listIds: this.selectedListIds(),
      tagIds:  this.selectedTagIds(),
      notes:   this.notes.trim(),
    });
  }

  public cancel(): void {
    this.ref.close();
  }
}