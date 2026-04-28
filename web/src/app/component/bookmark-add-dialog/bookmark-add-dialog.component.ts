import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';

import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipListboxChange, MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { FilterStateService } from '../../service/filter-state.service';

export interface BookmarkAddDialogResult {
    url: string;
    notes: string;
    listIds: number[];
    tagIds: number[];
}

@Component({
    selector: 'app-bookmark-add-dialog',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatCheckboxModule,
        MatChipsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
    ],
    templateUrl: './bookmark-add-dialog.component.html',
    styleUrl: './bookmark-add-dialog.component.scss',
})
export class BookmarkAddDialogComponent {
    private readonly dialogRef   = inject(MatDialogRef<BookmarkAddDialogComponent>);
    private readonly formBuilder = inject(FormBuilder);

    public readonly state = inject(FilterStateService);

    public readonly form = this.formBuilder.group({
        url: ['', [Validators.required, Validators.pattern(/^https?:\/\/.+/)]],
    });

    public notes  = '';
    public selectedListIds: number[] = [];
    public selectedTagIds:  number[] = [];

    get urlError(): string {
        const ctrl = this.form.get('url');

        if (ctrl?.hasError('required')) return 'URL is required';
        if (ctrl?.hasError('pattern'))  return 'Must be a valid URL (http or https)';
        
        return '';
    }

    public toggleList(id: number, checked: boolean): void {
        if (checked) {
            this.selectedListIds = [...this.selectedListIds, id];
        } else {
            this.selectedListIds = this.selectedListIds.filter(i => i !== id);
        }
    }

    public isListSelected(id: number): boolean {
        return this.selectedListIds.includes(id);
    }

    public onTagsChange(event: MatChipListboxChange): void {
        this.selectedTagIds = event.value ?? [];
    }

    public save(): void {
        if (this.form.invalid) return;

        this.dialogRef.close({
            url:     this.form.value.url!,
            notes:   this.notes.trim(),
            listIds: this.selectedListIds,
            tagIds:  this.selectedTagIds,
        } satisfies BookmarkAddDialogResult);
    }

    public cancel(): void {
        this.dialogRef.close();
    }
}