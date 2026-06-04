import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';

import { FilterStateService } from '../../service/filter-state.service';
import { Tag } from '../../model/tag.model';
import { buildTagTree, flattenTagTree } from '../../model/tag-tree.model';

export interface TagEditDialogData {
  tag?: Tag;
}

export interface TagEditDialogResult {
  name?:            string;
  parentId?:        number;
  backgroundColor?: string;
  textColor?:       string;
}

@Component({
  selector: 'tag-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatChipsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    ReactiveFormsModule
  ],
  templateUrl: './tag-edit-dialog.component.html',
  styleUrl:    './tag-edit-dialog.component.scss',
})
export class TagEditDialogComponent {
  private readonly DEFAULT_BACKGROUND_COLOR = '#cacaca';
  private readonly DEFAULT_TEXT_COLOR       = '#000000';

  private readonly data        = inject(MAT_DIALOG_DATA) as TagEditDialogData;
  private readonly dialogRef   = inject(MatDialogRef<TagEditDialogComponent>);
  private readonly formBuilder = inject(FormBuilder);
  private readonly state       = inject(FilterStateService);

  public readonly form = this.formBuilder.group({
    name: [
      this.data.tag?.name ?? '', [
        Validators.required,
        Validators.pattern(/^(?!\s*$).+/)
      ]
    ],
    parentId:        [this.data.tag?.parentId        ?? null],
    backgroundColor: [this.data.tag?.backgroundColor ?? this.DEFAULT_BACKGROUND_COLOR],
    textColor:       [this.data.tag?.textColor       ?? this.DEFAULT_TEXT_COLOR],
  });

  public readonly flatTagList = computed(() => flattenTagTree(buildTagTree(this.state.tags())));

  public readonly parentSearch = signal(
    this.flatTagList().find(i => i.tag.id === (this.data.tag?.parentId ?? -1))?.fullPath ?? ''
  );

  public readonly parentSuggestions = computed(() => {
    const input = this.parentSearch().trim().toLowerCase();
    
    const suggestions = this.flatTagList()
      .filter(i => !input || i.fullPath.toLowerCase().includes(input));
    
    const tag = this.data.tag;

    if (!tag) {
      return suggestions;
    }

    const excluded = new Set<number>([
      ...(tag ? [tag.id] : []),
      ...this.getDescendantIds(tag),
    ]);

    return suggestions.filter(i => !excluded.has(i.tag.id));
  });

  public readonly otherTags = computed(() =>
    this.state.tags().filter(tag => tag.id !== this.data.tag?.id)
  );

  public readonly isUnchanged = computed(() => {
    if (!this.isEdit()) return false;

    const tag = this.data.tag!;

    const { name, backgroundColor, textColor } = this.form.getRawValue();

    const sameName       = name && this.isSameName(name);
    const sameBackground = backgroundColor === tag.backgroundColor;
    const sameColor      = textColor === tag.textColor;

    return sameName && sameBackground && sameColor;
  });

  get nameError(): string {
    const control = this.form.get('name');

    if (control?.hasError('required')) return 'Name is required';
    if (control?.hasError('pattern'))  return 'Must be a valid name';

    return '';
  }

  public isEdit(): boolean {
    return !!this.data.tag;
  }

  public onParentSelected(event: MatAutocompleteSelectedEvent): void {
    const item = this.parentSuggestions().find(i => i.fullPath === event.option.value);

    this.form.patchValue({ parentId: item?.tag.id ?? null });
    this.parentSearch.set(event.option.value);
  }

  public clearParent(): void {
    this.form.patchValue({ parentId: null });
    this.parentSearch.set('');
  }

  public save(): void {
    if (this.form.invalid)  { this.form.markAllAsTouched(); return; }
    if (this.isUnchanged()) { this.dialogRef.close(); return; }

    const { name, parentId, backgroundColor, textColor } = this.form.getRawValue();

    this.dialogRef.close({
      name:            this.isEdit() && name && this.isSameName(name) ? undefined : name!.trim(),
      parentId:        parentId        ?? undefined,
      backgroundColor: backgroundColor ?? undefined,
      textColor:       textColor       ?? undefined,
    } satisfies TagEditDialogResult);
  }

  public resetColors(): void {
    this.form.patchValue({
      backgroundColor: this.DEFAULT_BACKGROUND_COLOR,
      textColor:       this.DEFAULT_TEXT_COLOR
    });
  }

  public cancel(): void {
    this.dialogRef.close();
  }

  private getDescendantIds(tag: Tag): number[] {
    if (!tag || !tag.children?.length) return [];
    
    return tag.children.flatMap(c => [c.id, ...this.getDescendantIds(c)]);
  }

  private isSameName(name: string): boolean {
    return name.trim().toLowerCase() === this.data.tag?.name.toLowerCase();
  }
}