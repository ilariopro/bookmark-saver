import { Component, inject } from '@angular/core';

import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

export interface TagDeleteDialogData {
    title: string;
    name:  string;
}

@Component({
    selector: 'tag-delete-dialog',
    standalone: true,
    imports: [
        MatButtonModule,
        MatDialogModule
    ],
    templateUrl: './tag-delete-dialog.component.html',
    styleUrl:    './tag-delete-dialog.component.scss',
})
export class TagDeleteDialogComponent {
    private readonly dialogRef = inject(MatDialogRef<TagDeleteDialogComponent>);
    public  readonly data      = inject(MAT_DIALOG_DATA) as TagDeleteDialogData;

    public confirm(): void {
        this.dialogRef.close(true);
    }

    public cancel(): void  {
        this.dialogRef.close(false);
    }
}