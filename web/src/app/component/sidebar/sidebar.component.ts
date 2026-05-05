import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatChipListboxChange, MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';

import { FilterStateService } from '../../service/filter-state.service';
import { BookmarkApiService } from '../../service/bookmark-api.service';
import { Tag } from '../../model/tag.model';
import { ListFormDialogComponent, ListFormDialogResult } from '../list-form-dialog/list-form-dialog.component';
import { TagFormDialogComponent, TagFormDialogResult } from '../tag-form-dialog/tag-form-dialog.component';
import { CommonDeleteDialogComponent, CommonDeleteDialogData } from '../common-delete-dialog/common-delete-dialog.component';
import { ApiList, DefaultListId } from '../../model/sidebar.model';

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
  public readonly state  = inject(FilterStateService);
  public readonly api    = inject(BookmarkApiService);
  public readonly dialog = inject(MatDialog);

  onSelectDefaultList(id: DefaultListId): void {
    this.state.selectDefaultList(id);
  }

  onSelectApiList(id: number): void {
    this.state.selectApiList(id);
  }

  onTagsChange(event: MatChipListboxChange): void {
    this.state.setSelectedTags(event.value ?? []);
  }

  // ── List actions ──────────────────────────────────────────────

  openCreateListDialog(): void {
    const ref = this.dialog.open(ListFormDialogComponent, {
      data: {},
      width: '440px',
    });

    ref.afterClosed().subscribe((result: ListFormDialogResult | undefined) => {
      if (!result) return;

      this.api
        .createList({
          name: result.name,
          description: result.description
        })
        .subscribe(list => this.state.apiLists.update(prev => [...prev, list]));
    });
  }

  openEditListDialog(list: ApiList): void {
    const ref = this.dialog.open(ListFormDialogComponent, {
      data: { list },
      width: '440px',
    });

    ref.afterClosed().subscribe((result: ListFormDialogResult | undefined) => {
      if (!result) return;

      this.api
        .updateList(list.id, result)
        .subscribe(updated =>
          this.state.apiLists.update(prev => prev.map(l => l.id === updated.id ? updated : l))
        );
    });
  }

  openDeleteListDialog(list: ApiList): void {
    const ref = this.dialog.open(CommonDeleteDialogComponent, {
      data: {
        title: 'Delete List',
        name: list.name,
        type: 'list'
      } satisfies CommonDeleteDialogData,
      width: '440px',
    });

    ref.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) return;

      this.api.deleteList(list.id).subscribe(() => {
        this.state.apiLists.update(prev => prev.filter(l => l.id !== list.id));

        // if (this.state.selectedListKey() === list.id) {
        //   this.state.selectList('all');
        // }
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

      this.api.createTag({ name: result.name }).subscribe(() =>
        this.api.getTags().subscribe(tags => this.state.tags.set(tags))
      );
    });
  }

  openEditTagDialog(tag: Tag): void {
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

  openDeleteTagDialog(tag: Tag): void {
    const ref = this.dialog.open(CommonDeleteDialogComponent, {
      data: {
        title: 'Delete Tag',
        name: tag.name,
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