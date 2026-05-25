import { Component, inject, input, output, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';

import { BookmarkApiService } from '../../service/bookmark-api.service';
import { NotificationService } from '../../service/notification.service';
import { BulkSelectDialogComponent, BulkSelectDialogResult } from '../bulk-select-dialog/bulk-select-dialog.component';
import { ResponsiveStateService } from '../../service/responsive-state.service';
import { BookmarkDeleteDialogComponent, BookmarkDeleteDialogData } from '../bookmark-delete-dialog/bookmark-delete-dialog.component';

@Component({
    selector: 'bulk-actions-bar',
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatDividerModule,
        MatIconModule,
        MatMenuModule,
        MatTooltipModule,
    ],
    templateUrl: './bulk-action-bar.component.html',
    styleUrl:    './bulk-action-bar.component.scss',
})
export class BulkActionBarComponent {
    private readonly api        = inject(BookmarkApiService);
    private readonly dialog     = inject(MatDialog);
    private readonly notify     = inject(NotificationService);
    private readonly responsive = inject(ResponsiveStateService);

    public readonly selectedIds = input.required<number[]>();
    public readonly done        = output<void>();

    get count(): number {
        return this.selectedIds().length;
    }

    get hasSelection(): boolean {
        return this.count > 0;
    }

    get isMobile(): boolean {
        return this.responsive.isMobile();
    }

    public bulkFavorite(favorite: boolean): void {
        this.api.bulkUpdate({ ids: this.selectedIds(), favorite }).subscribe({
            next: () => {
                this.notify.success(favorite ? `${this.count} bookmarks added to favorites` : `${this.count} bookmarks removed from favorites`);
                this.done.emit();
            },
            error: () => this.notify.error('Bulk update failed.'),
        });
    }

    public bulkArchive(archived: boolean): void {
        this.api.bulkUpdate({ ids: this.selectedIds(), archived }).subscribe({
            next: () => {
                this.notify.success(archived ? `${this.count} bookmarks archived` : `${this.count} bookmarks unarchived`);
                this.done.emit();
            },
            error: () => this.notify.error('Bulk update failed.'),
        });
    }

    public openTagsDialog(): void {
        const ref = this.dialog.open(BulkSelectDialogComponent, {
            data: { mode: 'tags' },
            width: '440px',
        });

        ref.afterClosed().subscribe((result: BulkSelectDialogResult | undefined) => {
            if (!result?.tagIds?.length) return;

            this.api.bulkUpdate({ ids: this.selectedIds(), tagIds: result.tagIds }).subscribe({
                next: () => {
                    this.notify.success(`${this.count} bookmarks tagged`);
                    this.done.emit();
                },
                error: () => this.notify.error('Bulk update failed.'),
            });
        });
    }

    public openDeleteDialog(): void {
        const ref = this.dialog.open(BookmarkDeleteDialogComponent, {
            data: {
                url: '',
                description: `Are you sure you want to delete ${this.count} bookmark${this.count > 1 ? 's' : ''}? This action cannot be undone.`,
            } satisfies BookmarkDeleteDialogData,
            width: '440px',
        });

        ref.afterClosed().subscribe((confirmed: boolean) => {
            if (!confirmed) return;

            this.bulkDelete();
        });
    }

    public bulkDelete(): void {
        this.api.bulkDelete(this.selectedIds()).subscribe({
            next: () => {
                this.notify.success(`${this.count} bookmarks deleted`);
                this.done.emit();
            },
            error: () => this.notify.error('Bulk delete failed.'),
        });
    }
}