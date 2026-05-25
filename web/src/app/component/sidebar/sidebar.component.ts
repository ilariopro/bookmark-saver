import { Component, inject, output } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatButtonModule } from '@angular/material/button';
import { MatChipListboxChange } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';

import { FilterStateService } from '../../service/filter-state.service';
import { BookmarkApiService } from '../../service/bookmark-api.service';
import { Tag } from '../../model/tag.model';
import { TagFormDialogComponent, TagFormDialogResult } from '../tag-form-dialog/tag-form-dialog.component';
import { TagDeleteDialogComponent, TagDeleteDialogData } from '../tag-delete-dialog/tag-delete-dialog.component';
import { DefaultListId } from '../../model/sidebar.model';
import { ResponsiveStateService } from '../../service/responsive-state.service';
import { SidebarTagTreeComponent } from '../sidebar-tag-tree/sidebar-tag-tree.component';

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
    MatTooltipModule,
    SidebarTagTreeComponent,
  ],
  templateUrl: './sidebar.component.html',
  styleUrl:    './sidebar.component.scss',
})
export class AppSidebar {
  public readonly api        = inject(BookmarkApiService);
  public readonly dialog     = inject(MatDialog);
  public readonly responsive = inject(ResponsiveStateService);
  public readonly state      = inject(FilterStateService);

  public readonly closeRequested = output<void>();

  public onSelectDefaultList(id: DefaultListId): void {
    this.state.selectDefaultList(id);
  }

  public onSelectTag(id: number): void {
    this.state.selectTag(id);
  }

  public onTagsChange(event: MatChipListboxChange): void {
    this.state.setSelectedTags(event.value ?? []);
  }

  // ── Tag actions ───────────────────────────────────────────────

  public openCreateTagDialog(): void {
    const ref = this.dialog.open(TagFormDialogComponent, {
      data: {},
      width: '440px',
    });

    ref.afterClosed().subscribe((result: TagFormDialogResult | undefined) => {
      if (!result) return;

      this.api.createTag({ name: result.name }).subscribe(() =>
        this.api.getTags().subscribe(tags => this.state.tags.set(tags))
      );
    });
  }

  public openEditTagDialog(tag: Tag): void {
    const ref = this.dialog.open(TagFormDialogComponent, {
      data: { tag },
      width: '440px',
    });

    ref.afterClosed().subscribe((result: TagFormDialogResult | undefined) => {
      if (!result) return;

      this.api.updateTag(tag.id, { name: result.name }).subscribe(() =>
        this.api.getTags().subscribe(tags => this.state.tags.set(tags))
      );
    });
  }

  public openDeleteTagDialog(tag: Tag): void {
    const ref = this.dialog.open(TagDeleteDialogComponent, {
      data: {
        title: 'Delete Tag',
        name: tag.name,
      } satisfies TagDeleteDialogData,
      width: '440px',
    });

    ref.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;

      this.api.deleteTag(tag.id).subscribe(() => {
        this.state.tags.update(prev => prev.filter(t => t.id !== tag.id));

        this.state.setSelectedTags(
          this.state.selectedTagIdsArray().filter(id => id !== tag.id)
        );
      });
    });
  }
}