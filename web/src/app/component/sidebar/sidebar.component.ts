import { Component, inject, output } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';

import { FilterStateService } from '../../service/filter-state.service';
import { BookmarkApiService } from '../../service/bookmark-api.service';
import { TagEditDialogComponent } from '../tag-edit-dialog/tag-edit-dialog.component';
import { DefaultListId } from '../../model/sidebar.model';
import { NotificationService } from '../../service/notification.service';
import { ResponsiveStateService } from '../../service/responsive-state.service';
import { Tag } from '../../model/tag.model';
import { TagDeleteDialogComponent } from '../tag-delete-dialog/tag-delete-dialog.component';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatDialogModule,
    MatDividerModule,
    MatIconModule,
    MatListModule,
    MatMenuModule,
    MatTooltipModule,
  ],
  templateUrl: './sidebar.component.html',
  styleUrl:    './sidebar.component.scss',
})
export class AppSidebar {
  private readonly api        = inject(BookmarkApiService);
  private readonly dialog     = inject(MatDialog);
  private readonly notify     = inject(NotificationService);
  public  readonly responsive = inject(ResponsiveStateService);
  public  readonly state      = inject(FilterStateService);

  public readonly closeRequested = output<void>();

  public isTagSelected(tag: Tag): boolean {
    const selection = this.state.selectedList();

    return selection?.type === 'tag'
        && selection.slug === tag.slug;
  }

  public onSelectTag(slug: string): void {
    this.state.selectTag(slug);
  }

  public onSelectDefaultList(id: DefaultListId): void {
    this.state.selectDefaultList(id);
  }

  public openCreateTagDialog(): void {
    this.dialog.open(TagEditDialogComponent, {
      data: {},
      width: '440px',
    });
  }

  public openEditTagDialog(tag: Tag): void {
    this.dialog.open(TagEditDialogComponent, {
      data: { tag },
      width: '440px',
    });
  }

  public openDeleteTagDialog(tag: Tag): void {
    const ref = this.dialog.open(TagDeleteDialogComponent, {
      data: { tag },
      width: '440px',
    });

    ref.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;
      
      this.api.deleteTag(tag.id)
        .subscribe({
          next: () => {
            this.api.getTags().subscribe(tags => this.state.tags.set(tags));
            this.notify.success('Tag deleted');
          },
          error: error => {
            this.notify.error(error?.error?.detail ?? 'An error occurred');
          }
        });
    });
  }
}