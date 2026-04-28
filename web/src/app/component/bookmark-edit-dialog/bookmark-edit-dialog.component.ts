import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipListboxChange, MatChipsModule } from '@angular/material/chips';

import { Bookmark } from '../../model/bookmark.model';
import { FilterStateService } from '../../service/filter-state.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

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
    MatInputModule,
  ],
  templateUrl: './bookmark-edit-dialog.component.html',
  styleUrl: './bookmark-edit-dialog.component.scss',
})
export class BookmarkEditDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<BookmarkEditDialogComponent>);

  public readonly data: BookmarkEditDialogData = inject(MAT_DIALOG_DATA);
  public readonly state = inject(FilterStateService);

  public notes           = this.data.bookmark.notes ?? '';
  public selectedListIds = this.data.bookmark.lists.map(list => list.id);
  public selectedTagIds  = this.data.bookmark.tags.map(tag => tag.id);

  public onListsChange(event: MatChipListboxChange): void {
    this.selectedListIds = event.value ?? [];
  }

  public onTagsChange(event: MatChipListboxChange): void {
    this.selectedTagIds = event.value ?? [];
  }

  public isListSelected(id: number): boolean {
    return this.selectedListIds.includes(id);
  }

  public toggleList(id: number, checked: boolean): void {
    if (checked) {
      this.selectedListIds = [...this.selectedListIds, id];
    } else {
      this.selectedListIds = this.selectedListIds.filter(listId => listId !== id);
    }
  }

  public save(): void {
    this.dialogRef.close({
      listIds: this.selectedListIds,
      tagIds:  this.selectedTagIds,
      notes:   this.notes.trim(),
    });
  }

  public cancel(): void {
    this.dialogRef.close();
  }
}