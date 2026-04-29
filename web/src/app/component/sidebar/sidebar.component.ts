import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatChipListboxChange, MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog } from '@angular/material/dialog';
import { MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ListFormDialogComponent, ListFormDialogResult } from '../list-form-dialog/list-form-dialog.component';
import { TagFormDialogComponent, TagFormDialogResult } from '../tag-form-dialog/tag-form-dialog.component';
import { FilterStateService } from '../../service/filter-state.service';
import { BookmarkApiService } from '../../service/bookmark-api.service';
import { SidebarList } from '../../model/shared.model';
import { Tag } from '../../model/tag.model';
import { CommonDeleteDialogComponent, CommonDeleteDialogData } from '../common-delete-dialog/common-delete-dialog.component';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatChipsModule,
    MatDialogModule,
    MatDividerModule,
    MatIconModule,
    MatListModule,
    MatMenuModule,
    MatTooltipModule,
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class AppSidebar {
  readonly state  = inject(FilterStateService);
  readonly api    = inject(BookmarkApiService);
  readonly dialog = inject(MatDialog);

  readonly isEdit = signal(false);

  toggleEditMode(): void {
    this.isEdit.set(!this.isEdit());
  }

  // ── List actions ──────────────────────────────────────────────

  onListClick(): void {
    if (!this.isEdit()) this.state.setSelectedTags([]);
  }

  onListChange(listId: string): void {
    if (!this.isEdit()) this.state.selectList(listId);
  }

  onTagsChange(event: MatChipListboxChange): void {
    this.state.setSelectedTags(event.value ?? []);
  }

  openCreateListDialog(): void {
    const ref = this.dialog.open(ListFormDialogComponent, {
      data: {},
      width: '440px',
    });

    ref.afterClosed().subscribe((result: ListFormDialogResult | undefined) => {
      if (!result) return;

      const { name, description } = result;

      this.api.createList({ name, description }).subscribe(list => {
        this.state.apiLists.update(prev => [...prev, list]);
      });
    });
  }

  openEditListDialog(sidebarList: SidebarList): void {
    const list = this.state.resolveList(sidebarList);

    if (!list) return;

    const ref = this.dialog.open(ListFormDialogComponent, {
      data: { list },
      width: '440px',
    });

    ref.afterClosed().subscribe((result: ListFormDialogResult | undefined) => {
      if (!result) return;

      this.api.updateList(list.id, result).subscribe(updated => {
        this.state.apiLists.update(prev => prev.map(list => list.id === updated.id ? updated : list));
      });
    });
  }

  openDeleteListDialog(sidebarList: SidebarList): void {
    const list = this.state.resolveList(sidebarList);

    if (!list) return;

    const ref = this.dialog.open(CommonDeleteDialogComponent, {
      data: {
        name: list.name,
        title: 'Delete List',
        type: 'list'
       } satisfies CommonDeleteDialogData,
      width: '440px',
    });

    ref.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;

      this.api.deleteList(list.id).subscribe(() => {
        this.state.apiLists.update(prev => prev.filter(l => l.id !== list.id));
        
        if (this.state.selectedListKey() === sidebarList.id) {
          this.state.selectList('all');
        }
      });
    });
  }

  // ── Tag actions ───────────────────────────────────────────────

  openCreateTagDialog(): void {
    const ref = this.dialog.open(TagFormDialogComponent, {
      data: {},
      width: '440px',
    });
    
    ref.afterClosed().subscribe((result: TagFormDialogResult | undefined) => {
      if (!result) return;

      this.api.createTag({ name: result.name }).subscribe(() => {
        this.api.getTags().subscribe(tags => this.state.tags.set(tags));
      });
    });
  }

  openEditTagDialog(tag: Tag): void {
    const ref = this.dialog.open(TagFormDialogComponent, {
      data: { tag },
      width: '440px',
    });

    ref.afterClosed().subscribe((result: TagFormDialogResult | undefined) => {
      if (!result) return;

      this.api.updateTag(tag.id, { name: result.name }).subscribe(() => {
        this.api.getTags().subscribe(tags => this.state.tags.set(tags));
      });
    });
  }

  openDeleteTagDialog(tag: Tag): void {
    const ref = this.dialog.open(CommonDeleteDialogComponent, {
      data: {
        name: tag.name,
        title: 'Delete Tag',
        type: 'tag'
      } satisfies CommonDeleteDialogData,
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