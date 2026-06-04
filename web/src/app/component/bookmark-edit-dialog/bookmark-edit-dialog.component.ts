import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';

import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipListboxChange, MatChipsModule } from '@angular/material/chips';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Bookmark, Metadata } from '../../model/bookmark.model';
import { FilterStateService } from '../../service/filter-state.service';
import { BookmarkApiService } from '../../service/bookmark-api.service';
import { TagEditDialogComponent, TagEditDialogResult } from '../tag-edit-dialog/tag-edit-dialog.component';
import { buildTagTree, flattenTagTree } from '../../model/tag-tree.model';

export interface BookmarkEditDialogData {
  bookmark?: Bookmark;
}

export interface BookmarkEditDialogResult {
  url?:   string;     // create
  notes:  string;
  tagIds: number[];
}

@Component({
  selector: 'bookmark-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatChipsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatTooltipModule,
    ReactiveFormsModule,
  ],
  templateUrl: './bookmark-edit-dialog.component.html',
  styleUrl:    './bookmark-edit-dialog.component.scss',
})
export class BookmarkEditDialogComponent {
  private readonly api         = inject(BookmarkApiService);
  private readonly data        = inject(MAT_DIALOG_DATA) as BookmarkEditDialogData;
  private readonly dialogRef   = inject(MatDialogRef<BookmarkEditDialogComponent>);
  private readonly dialog      = inject(MatDialog);
  private readonly formBuilder = inject(FormBuilder);
  private readonly state       = inject(FilterStateService);

  public readonly form = this.formBuilder.group({
    url: [
      this.data.bookmark?.url ?? '', [
        Validators.required,
        Validators.pattern(/^https?:\/\/.+/)
      ]
    ],
    notes: [
      this.data.bookmark?.notes ?? ''
    ],
    tagIds: [
      this.data.bookmark?.tags.map(tag => tag.id) ?? []
    ]
  });

  public readonly selectedTagIds = signal<number[]>(
    this.data.bookmark?.tags.map(tag => tag.id) ?? []
  );

  public readonly isUnchanged = computed(() => {
    if (!this.isEdit()) return false;

    const bookmark      = this.data.bookmark!;
    const initialTagIds = bookmark.tags.map(tag => tag.id).sort();

    const { url, notes, tagIds } = this.form.getRawValue();

    const sameUrl   = url === bookmark.url;
    const sameNotes = notes?.trim() === (bookmark.notes ?? '');

    const sameLength = tagIds?.length === initialTagIds.length;
    const sameTags   = tagIds?.every(id => initialTagIds.includes(id));

    return sameUrl && sameNotes && sameLength && sameTags;
  });

  public readonly selectedTags = computed(() =>
    this.state.tags().filter(t => this.tagIds.includes(t.id))
  );

  public readonly flatTagList = computed(() => flattenTagTree(buildTagTree(this.state.tags())));

  get metadata(): Metadata | null {
    return this.data.bookmark?.metadata ?? null;
  }

  get tagIds(): number[] {
    return (this.form.get('tagIds')?.value as number[]) ?? [];
  }

  get url(): string {
    return this.data.bookmark?.url ?? '';
  }

  get urlError(): string {
    const control = this.form.get('url');

    if (control?.hasError('required')) return 'URL is required';
    if (control?.hasError('pattern'))  return 'Must be a valid URL (http or https)';

    return '';
  }

  public isEdit(): boolean {
    return !!this.data.bookmark;
  }

  // ── Tags ──────────────────────────────────────────────────────

  public onTagsChange(event: MatChipListboxChange): void {
    this.selectedTagIds.set(event.value ?? []);
  }

  public openCreateTagDialog(): void {
    const ref = this.dialog.open(TagEditDialogComponent, { data: {}, width: '440px' });

    ref.afterClosed().subscribe((result: TagEditDialogResult | undefined) => {
      if (!result) return;
      
      this.api.createTag({
        name:            result.name,
        parentId:        result.parentId,
        backgroundColor: result.backgroundColor,
        textColor:       result.textColor,
      }).subscribe(tag => {
        this.api.getTags().subscribe(tags => {
          this.state.tags.set(tags);
          this.selectedTagIds.update(prev => [...prev, tag.id]);
        });
      });
    });
  }

  public removeTag(tagId: number): void {
    this.selectedTagIds.update(prev => prev.filter(id => id !== tagId));
  }

  public toggleTag(id: number, checked: boolean): void {
    const current = this.tagIds;

    const updated = checked
      ? [...current, id]
      : current.filter(i => i !== id);

    this.form.get('tagIds')?.setValue(updated); 
  }

  // ── Actions ───────────────────────────────────────────────────

  public save(): void {
    if (!this.isEdit() && this.form.invalid)  { this.form.markAllAsTouched(); return; }
    if (this.isEdit()  && this.isUnchanged()) { this.dialogRef.close(); return; }

    const { url, notes, tagIds } = this.form.getRawValue();

    this.dialogRef.close({
      url:     this.isEdit() ? undefined : url!,
      notes:   notes?.trim() ?? '',
      tagIds:  tagIds ?? [],
    } satisfies BookmarkEditDialogResult);
  }

  public cancel(): void {
    this.dialogRef.close();
  }
}