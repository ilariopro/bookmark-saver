import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatIconModule } from '@angular/material/icon';

import { FilterStateService } from '../../service/filter-state.service';
import { Tag } from '../../model/tag.model';

export interface TagFormDialogData {
  tag?: Tag; // presente = edit, assente = create
}

export interface TagFormDialogResult {
  name: string;
}

@Component({
  selector: 'app-tag-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
  ],
  templateUrl: './tag-form-dialog.component.html',
  styleUrl: './tag-form-dialog.component.scss',
})
export class TagFormDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<TagFormDialogComponent>);
  readonly data: TagFormDialogData = inject(MAT_DIALOG_DATA);
  readonly state = inject(FilterStateService);

  readonly name = signal(this.data.tag?.name ?? '');

  readonly otherTags = computed(() =>
    this.state.tags().filter(tag => tag.id !== this.data.tag?.id)
  );

  readonly suggestions = computed(() => {
    const input = this.name().trim().toLowerCase();

    if (!input) return [];

    return this.otherTags().filter(tag => tag.name.toLowerCase().includes(input));
  });

  readonly isMerge = computed(() =>
    this.otherTags().some(tag => tag.name.toLowerCase() === this.name().trim().toLowerCase())
  );

  readonly isUnchanged = computed(() =>
    this.isEdit() && this.name().trim().toLowerCase() === this.data.tag!.name.toLowerCase()
  );

  readonly isInvalid = computed(() =>
    !this.name().trim() || this.isUnchanged()
  );

  isEdit(): boolean {
    return !!this.data.tag;
  }

  onNameChange(value: string): void {
    this.name.set(value);
  }

  save(): void {
    if (this.isInvalid()) return;
    this.dialogRef.close({ name: this.name().trim() } satisfies TagFormDialogResult);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}