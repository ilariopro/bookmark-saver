import { Component, inject } from '@angular/core';

import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

import { Tag } from '../../model/tag.model';

export interface TagDeleteDialogData {
    tag: Tag;
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
    private readonly dialogRef  = inject(MatDialogRef<TagDeleteDialogComponent>);
    private readonly dialogData = inject(MAT_DIALOG_DATA) as TagDeleteDialogData;

    public get data() {
        return this.dialogData.tag;
    }

    public confirm(): void {
        this.dialogRef.close(true);
    }

    public cancel(): void  {
        this.dialogRef.close(false);
    }
}