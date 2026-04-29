import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

export interface CommonDeleteDialogData {
    title: string;
    name: string;
    type: 'list' | 'tag';
}

@Component({
    selector: 'app-common-delete-dialog',
    standalone: true,
    imports: [
        MatButtonModule,
        MatDialogModule
    ],
    templateUrl: './common-delete-dialog.component.html',
    styleUrl: './common-delete-dialog.component.scss',
})
export class CommonDeleteDialogComponent {
    private readonly dialogRef = inject(MatDialogRef<CommonDeleteDialogComponent>);
    public readonly data: CommonDeleteDialogData = inject(MAT_DIALOG_DATA);

    public confirm(): void {
        this.dialogRef.close(true);
    }

    public cancel(): void  {
        this.dialogRef.close(false);
    }
}