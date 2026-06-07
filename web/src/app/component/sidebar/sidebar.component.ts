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
import { TagEditDialogComponent, TagEditDialogResult } from '../tag-edit-dialog/tag-edit-dialog.component';
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

  public onTagsChange(event: MatChipListboxChange): void {
    this.state.setSelectedTags(event.value ?? []);
  }

  public openCreateTagDialog(): void {
    const ref = this.dialog.open(TagEditDialogComponent, {
      data: {},
      width: '440px',
    });

    ref.afterClosed().subscribe((result: TagEditDialogResult | undefined) => {
      if (!result) return;

      this.api.createTag({
        name:            result.name,
        slug:            result.slug,
        parentId:        result.parentId,
        backgroundColor: result.backgroundColor,
        textColor:       result.textColor,
      }).subscribe(() =>
        this.api.getTags().subscribe(tags => this.state.tags.set(tags))
      );
    });
  }
}