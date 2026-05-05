import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';

import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipListboxChange, MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Bookmark } from '../../model/bookmark.model';
import { FilterStateService } from '../../service/filter-state.service';
import { BookmarkApiService } from '../../service/bookmark-api.service';
import { ListFormDialogComponent, ListFormDialogResult } from '../list-form-dialog/list-form-dialog.component';
import { TagFormDialogComponent, TagFormDialogResult } from '../tag-form-dialog/tag-form-dialog.component';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatExpansionModule } from '@angular/material/expansion';

export interface BookmarkFormDialogData {
  bookmark?: Bookmark;
}

export interface BookmarkFormDialogResult {
  url?: string;        // create
  notes: string;
  listIds: number[];
  tagIds: number[];
}

@Component({
  selector: 'app-bookmark-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatCheckboxModule,
    MatChipsModule,
    MatDialogModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatTooltipModule,
  ],
  templateUrl: './bookmark-form-dialog.component.html',
  styleUrl: './bookmark-form-dialog.component.scss',
})
export class BookmarkFormDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<BookmarkFormDialogComponent>);
  private readonly dialog    = inject(MatDialog);
  private readonly fb        = inject(FormBuilder);
  private readonly api       = inject(BookmarkApiService);

  readonly data: BookmarkFormDialogData = inject(MAT_DIALOG_DATA);
  readonly state = inject(FilterStateService);

  // solo in create
  readonly urlForm = this.fb.group({
    url: ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/)]],
  });

  // stato condiviso
  readonly notes           = signal(this.data.bookmark?.notes ?? '');
  readonly selectedListIds = signal<number[]>(this.data.bookmark?.lists.map(l => l.id) ?? []);
  readonly selectedTagIds  = signal<number[]>(this.data.bookmark?.tags.map(t => t.id) ?? []);
  readonly tagSearch       = signal('');

  readonly isUnchanged = computed(() => {
    if (!this.isEdit()) return false;

    const originalListIds = new Set(this.data.bookmark!.lists.map(l => l.id));
    const originalTagIds  = new Set(this.data.bookmark!.tags.map(t => t.id));

    const sameNotes = this.notes().trim() === (this.data.bookmark!.notes ?? '');
    const sameLists = this.selectedListIds().length === originalListIds.size &&
                      this.selectedListIds().every(id => originalListIds.has(id));
    const sameTags  = this.selectedTagIds().length === originalTagIds.size &&
                      this.selectedTagIds().every(id => originalTagIds.has(id));

    return sameNotes && sameLists && sameTags;
  });

  readonly selectedTags = computed(() =>
    this.state.tags().filter(t => this.selectedTagIds().includes(t.id))
  );

  readonly tagSuggestions = computed(() => {
    const input     = this.tagSearch().trim().toLowerCase();
    const selected  = new Set(this.selectedTagIds());
    const available = this.state.tags().filter(tag => !selected.has(tag.id));

    if (!input) return available;
    
    return available.filter(tag => tag.name.toLowerCase().includes(input));
  });

  get urlError(): string {
    const control = this.urlForm.get('url');

    if (control?.hasError('required')) return 'URL is required';
    if (control?.hasError('pattern'))  return 'Must be a valid URL (http or https)';

    return '';
  }

  // ── Lists ─────────────────────────────────────────────────────

  isEdit(): boolean {
    return !!this.data.bookmark;
  }

  isListSelected(id: number): boolean {
    return this.selectedListIds().includes(id);
  }

  toggleList(id: number, checked: boolean): void {
    this.selectedListIds.update(prev =>
      checked ? [...prev, id] : prev.filter(i => i !== id)
    );
  }

  openCreateListDialog(): void {
    const ref = this.dialog.open(ListFormDialogComponent, { data: {}, width: '440px' });
    
    ref.afterClosed().subscribe((result: ListFormDialogResult | undefined) => {
      if (!result) return;

      this.api.createList({ name: result.name, description: result.description })
        .subscribe(list => {
          this.api.getLists().subscribe(lists => {
            this.state.apiLists.set(lists);
            this.selectedListIds.update(prev => [...prev, list.id]);
          });
        });
    });
  }

  // ── Tags ──────────────────────────────────────────────────────

  onTagsChange(event: MatChipListboxChange): void {
    this.selectedTagIds.set(event.value ?? []);
  }

  openCreateTagDialog(): void {
    const ref = this.dialog.open(TagFormDialogComponent, { data: {}, width: '440px' });

    ref.afterClosed().subscribe((result: TagFormDialogResult | undefined) => {
      if (!result) return;
      
      this.api.createTag({ name: result.name }).subscribe(tag => {
        this.api.getTags().subscribe(tags => {
          this.state.tags.set(tags);
          this.selectedTagIds.update(prev => [...prev, tag.id]);
          this.tagSearch.set('');
        });
      });
    });
  }

  addTag(event: MatAutocompleteSelectedEvent): void {
    const tag = this.state.tags().find(tag => tag.name === event.option.value);
    
    if (!tag) return;
    
    this.selectedTagIds.update(prev => [...prev, tag.id]);
    this.tagSearch.set('');
  }

  removeTag(tagId: number): void {
    this.selectedTagIds.update(prev => prev.filter(id => id !== tagId));
  }

  // ── Actions ───────────────────────────────────────────────────

  save(): void {
    if (this.isEdit() && this.isUnchanged()) return;
    if (!this.isEdit() && this.urlForm.invalid) return;

    this.dialogRef.close({
      url:     this.isEdit() ? undefined : this.urlForm.value.url!,
      notes:   this.notes().trim(),
      listIds: this.selectedListIds(),
      tagIds:  this.selectedTagIds(),
    } satisfies BookmarkFormDialogResult);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}