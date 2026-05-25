import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';

import { FilterStateService } from '../../service/filter-state.service';
import { Tag } from '../../model/tag.model';

export interface TagFormDialogData {
  tag?: Tag;
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
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
  ],
  templateUrl: './tag-form-dialog.component.html',
  styleUrl:    './tag-form-dialog.component.scss',
})
export class TagFormDialogComponent {
  private readonly data      = inject(MAT_DIALOG_DATA) as TagFormDialogData;
  private readonly dialogRef = inject(MatDialogRef<TagFormDialogComponent>);
  private readonly state     = inject(FilterStateService);

  public readonly name = signal(this.data.tag?.name ?? '');

  public readonly otherTags = computed(() =>
    this.state.tags().filter(tag => tag.id !== this.data.tag?.id)
  );

  public readonly isMatch = computed(() =>
    this.otherTags().some(tag => tag.name.toLowerCase() === this.name().trim().toLowerCase())
  );

  public readonly isUnchanged = computed(() =>
    this.isEdit() && this.name().trim().toLowerCase() === this.data.tag!.name.toLowerCase()
  );

  public readonly isInvalid = computed(() =>
    !this.name().trim() || this.isUnchanged()
  );

  public isEdit(): boolean {
    return !!this.data.tag;
  }

  public canSave(): boolean {
    return this.isInvalid() || this.isMatch()
      ? false
      : true;
  }

  public onNameChange(value: string): void {
    this.name.set(value);
  }

  public save(): void {
    if (this.isInvalid()) return;

    this.dialogRef.close({ name: this.name().trim() } satisfies TagFormDialogResult);
  }

  public cancel(): void {
    this.dialogRef.close();
  }
}