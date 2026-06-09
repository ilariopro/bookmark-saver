import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';

import { FilterStateService } from '../../service/filter-state.service';

type TagDisplayState = 'indeterminate' | 'checked' | 'unchecked';

export interface BulkTagDialogData {
  initialTagIds: number[];
}

export interface BulkTagDialogResult {
  addTagIds:    number[];
  removeTagIds: number[];
}

@Component({
  selector: 'bulk-tag-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCheckboxModule,
    MatDialogModule,
  ],
  templateUrl: './bulk-tag-dialog.component.html',
  styleUrl:    './bulk-tag-dialog.component.scss',
})
export class BulkTagDialogComponent {
    private readonly dialogRef = inject(MatDialogRef<BulkTagDialogComponent>);
    public  readonly data      = inject(MAT_DIALOG_DATA) as BulkTagDialogData;
    public  readonly state     = inject(FilterStateService);

    private readonly initialSet = new Set(this.data.initialTagIds);

    // stato per ogni tag come array di record per triggare correttamente il re-render
    public readonly tagStates = signal<Record<number, TagDisplayState>>(
        Object.fromEntries(
            this.state.tags().map(tag => [
                tag.id,
                this.initialSet.has(tag.id) ? 'indeterminate' : 'unchecked'
            ])
        )
    );

    public readonly hasChanges = computed(() =>
        this.state.tags().some(tag => {
            const state = this.getState(tag.id);

            return this.initialSet.has(tag.id)
                ? state === 'unchecked'   // era present, ora rimosso
                : state === 'checked';    // era absent, ora aggiunto
        })
    );

    public getState(id: number): TagDisplayState {
        return this.tagStates()[id] ?? 'unchecked';
    }

    public isIndeterminate(id: number): boolean {
        return this.getState(id) === 'indeterminate';
    }

    public isChecked(id: number): boolean {
        return this.getState(id) === 'checked';
    }

    public onChange(id: number): void {
        const current = this.getState(id);

        if (current === 'checked') {
            this.tagStates.update(prev => ({ ...prev, [id]: 'unchecked' }));
            return;
        }

        this.tagStates.update(prev => ({ ...prev, [id]: 'checked' }));
    }

    public save(): void {
        const addTagIds:    number[] = [];
        const removeTagIds: number[] = [];

        this.state.tags().forEach(t => {
            const state = this.getState(t.id);

            if (!this.initialSet.has(t.id) && state === 'checked')   addTagIds.push(t.id);
            if (this.initialSet.has(t.id)  && state === 'unchecked') removeTagIds.push(t.id);
        });

        this.dialogRef.close({ addTagIds, removeTagIds });
    }

    public cancel(): void {
        this.dialogRef.close();
    }
}