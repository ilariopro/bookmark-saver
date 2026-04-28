import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

export interface BookmarkDeleteDialogData {
  title: string;
  url: string;
}

@Component({
    selector: 'app-bookmark-delete-dialog',
    standalone: true,
    imports: [
        MatButtonModule,
        MatDialogModule
    ],
    templateUrl: './bookmark-delete-dialog.component.html',
    styleUrl: './bookmark-delete-dialog.component.scss',
})
export class BookmarkDeleteDialogComponent {
    private readonly dialogRef = inject(MatDialogRef<BookmarkDeleteDialogComponent>);
    public readonly data: BookmarkDeleteDialogData = inject(MAT_DIALOG_DATA);

    public confirm(): void {
        this.dialogRef.close(true);
    }

    public cancel(): void  {
        this.dialogRef.close(false);
    }
}