import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipListboxChange, MatChipsModule } from '@angular/material/chips';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

import { FilterStateService } from '../../service/filter-state.service';

export interface BulkSelectDialogResult {
    tagIds?:  number[];
}

@Component({
    selector: 'app-bulk-select-dialog',
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatCheckboxModule,
        MatChipsModule,
        MatDialogModule,
    ],
    templateUrl: './bulk-select-dialog.component.html',
    styleUrl:    './bulk-select-dialog.component.scss',
})
export class BulkSelectDialogComponent {
    private readonly dialogRef = inject(MatDialogRef<BulkSelectDialogComponent>);
    readonly data = inject(MAT_DIALOG_DATA);
    readonly state = inject(FilterStateService);

    selectedListIds: number[] = [];
    selectedTagIds:  number[] = [];

    public toggleList(listId: number, checked: boolean): void {
        this.selectedListIds = checked
            ? [...this.selectedListIds, listId]
            : this.selectedListIds.filter(id => id !== listId);
    }

    public onTagsChange(event: MatChipListboxChange): void {
        this.selectedTagIds = event.value ?? [];
    }

    public save(): void {
        this.dialogRef.close({ tagIds:  this.selectedTagIds });
    }

    public cancel(): void {
        this.dialogRef.close();
    }
}