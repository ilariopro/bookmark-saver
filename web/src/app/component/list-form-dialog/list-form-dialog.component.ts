import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { List } from '../../model/list.model';

export interface ListFormDialogData {
  list?: List; // se presente = edit, assente = create
}

export interface ListFormDialogResult {
  name: string;
  description: string;
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
  public readonly data: ListFormDialogData = inject(MAT_DIALOG_DATA);

  public name        = this.data.list?.name        ?? '';
  public description = this.data.list?.description ?? '';

  public isEdit(): boolean {
    return !!this.data.list;
  }

  public save(): void {
    const name = this.name.trim();
    
    if (!name) return;

    this.dialogRef.close({ name, description: this.description.trim() } satisfies ListFormDialogResult);
  }

  public cancel(): void {
    this.dialogRef.close();
  }
}