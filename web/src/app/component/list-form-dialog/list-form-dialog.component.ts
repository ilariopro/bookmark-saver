import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { List } from '../../model/list.model';
import { ApiList } from '../../model/sidebar.model';

export interface ListFormDialogData {
  list?: ApiList;
}

export interface ListFormDialogResult {
  name: string;
  description: string;
  position?: number;
}

@Component({
  selector: 'app-list-form-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './list-form-dialog.component.html',
  styleUrl: './list-form-dialog.component.scss',
})
export class ListFormDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<ListFormDialogComponent>);
  private readonly data      = inject(MAT_DIALOG_DATA) as ListFormDialogData;

  public readonly name        = signal(this.data.list?.name ?? '');
  public readonly description = signal(this.data.list?.description ?? '');

  public readonly isUnchanged = computed(() =>
    this.isEdit()
      && this.name().trim() === (this.data.list!.name ?? '')
      && this.description().trim() === (this.data.list!.description ?? '')
  );

  public readonly canSave = computed(() =>
    !!this.name().trim() && !this.isUnchanged()
  );

  public onNameChange(value: string): void {
    this.name.set(value);
  }

  public onDescriptionChange(value: string): void {
    this.description.set(value);
  }

  public isEdit(): boolean {
    return !!this.data.list;
  }

  public save(): void {
    if (!this.canSave()) return;

    this.dialogRef.close({
      name:        this.name().trim(),
      description: this.description().trim(),
    } satisfies ListFormDialogResult);
  }

  public cancel(): void {
    this.dialogRef.close();
  }
}