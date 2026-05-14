import { Component, inject } from '@angular/core';

import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

export interface BookmarkDeleteDialogData {
    url: string;
    description: string;
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
    private readonly data      = inject(MAT_DIALOG_DATA) as BookmarkDeleteDialogData;
    private readonly dialogRef = inject(MatDialogRef<BookmarkDeleteDialogComponent>);

    get url(): string {
        return this.data.url;
    }

    get description(): string {
        return this.data.description;
    }

    public confirm(): void {
        this.dialogRef.close(true);
    }

    public cancel(): void  {
        this.dialogRef.close(false);
    }
}