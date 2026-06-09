import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';

import { FilterStateService } from '../../service/filter-state.service';
import { Tag, TagPayload } from '../../model/tag.model';
import { BookmarkApiService } from '../../service/bookmark-api.service';

export interface TagEditDialogData {
  tag?: Tag;
}

export interface TagEditDialogResult {
  tag?: Tag;
}

@Component({
  selector: 'tag-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
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
  private readonly api         = inject(BookmarkApiService);
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
    ]
  });

  public readonly loading = signal(false);

  public readonly otherTags = computed(() =>
    this.state.tags().filter(tag => tag.id !== this.data.tag?.id)
  );

  public readonly isUnchanged = computed(() => {
    if (!this.isEdit()) return false;

    const form = this.form.getRawValue();
    const tag  = this.data.tag!;

    return form.name!.trim() === tag.name
  });

  public get nameError(): string {
    const control = this.form.get('name');

    if (control?.hasError('required')) return 'Name is required';
    if (control?.hasError('pattern'))  return 'Must be a valid name';
    if (control?.hasError('server'))   return control.getError('server');

    return '';
  }

  public isEdit(): boolean {
    return !!this.data.tag;
  }

  public save(): void {
    if (this.form.invalid)  { this.form.markAllAsTouched(); return; }
    if (this.isUnchanged()) { this.dialogRef.close(); return; }

    this.loading.set(true);

    const apiCall = this.isEdit()
      ? this.api.updateTag(this.data.tag!.id, this.buildPayload())
      : this.api.createTag(this.buildPayload());

    apiCall.subscribe({
      next: tag => {
        this.api.getTags().subscribe(tags => this.state.tags.set(tags));
        this.dialogRef.close(tag);
      },
      error: error => {
        this.loading.set(false);
        
        const message = error?.error?.detail ?? 'An error occurred';

        this.form.get('name')?.setErrors({ server: message });
        this.form.get('name')?.markAsTouched();
      },
    });
  }

  public cancel(): void {
    this.dialogRef.close();
  }

  private buildPayload(): TagPayload {
    const form        = this.form.getRawValue();
    const nameChanged = form.name!.trim() !== this.data.tag?.name;

    return {
      name: form.name!.trim(),
      slug: this.isEdit() && !nameChanged ? undefined : this.createSlug(form.name!),
    };
  }

  private createSlug(name: string): string {
    return name
      .trim()
      .toLowerCase()
      .replace(/\s+/g, '-')
      .replace(/[^a-z0-9-_]/g, '');
  }
}