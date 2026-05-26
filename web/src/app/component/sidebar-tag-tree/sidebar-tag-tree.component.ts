import { Component, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FlatTreeControl } from '@angular/cdk/tree';
import { MatTreeModule, MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog } from '@angular/material/dialog';

import { Tag } from '../../model/tag.model';
import { TagNode } from '../../model/tag-tree.model';
import { FilterStateService } from '../../service/filter-state.service';
import { BookmarkApiService } from '../../service/bookmark-api.service';
import { TagEditDialogComponent, TagEditDialogResult } from '../tag-edit-dialog/tag-edit-dialog.component';
import { TagDeleteDialogComponent, TagDeleteDialogData } from '../tag-delete-dialog/tag-delete-dialog.component';

interface FlatTagNode {
    tag:        Tag;
    level:      number;
    expandable: boolean;
    fullPath:   string;
}

@Component({
  selector: 'sidebar-tag-tree',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatTreeModule,
  ],
  templateUrl: './sidebar-tag-tree.component.html',
  styleUrl:    './sidebar-tag-tree.component.scss',
})
export class SidebarTagTreeComponent {
    public readonly state  = inject(FilterStateService);
    public readonly api    = inject(BookmarkApiService);
    public readonly dialog = inject(MatDialog);

    private readonly flattener = new MatTreeFlattener<TagNode, FlatTagNode>(
        (node, level) => ({
            tag:        node.tag,
            level,
            expandable: node.children.length > 0,
            fullPath:   node.fullPath,
        }),
        node => node.level,
        node => node.expandable,
        node => node.children,
    );

    readonly treeControl = new FlatTreeControl<FlatTagNode>(
        node => node.level,
        node => node.expandable,
    );

    readonly dataSource = new MatTreeFlatDataSource(this.treeControl, this.flattener);

    constructor() {
        effect(() => {
            this.dataSource.data = this.state.tagTree();
        });
    }

    public hasChildren = (_: number, node: FlatTagNode): boolean => node.expandable;

    public isSelected(node: FlatTagNode): boolean {
        const sel = this.state.selectedList();

        return sel?.type === 'tag'
            && sel.id === node.tag.id;
    }

    public navigate(node: FlatTagNode): void {
        this.state.selectTag(node.tag.id);
    }

    public openEditDialog(node: FlatTagNode): void {
        const ref = this.dialog.open(TagEditDialogComponent, {
            data: { tag: node.tag },
            width: '440px',
        });

        ref.afterClosed().subscribe((result: TagEditDialogResult | undefined) => {
            if (!result) return;

            this.api.updateTag(node.tag.id, { name: result.name }).subscribe(() =>
                this.api.getTags().subscribe(tags => this.state.tags.set(tags))
            );
        });
    }

    public openDeleteDialog(node: FlatTagNode): void {
        const ref = this.dialog.open(TagDeleteDialogComponent, {
            data: { title: 'Delete Tag', name: node.tag.name } satisfies TagDeleteDialogData,
            width: '440px',
        });

        ref.afterClosed().subscribe((confirmed: boolean) => {
            if (!confirmed) return;

            this.api.deleteTag(node.tag.id).subscribe(() =>
                this.api.getTags().subscribe(tags => this.state.tags.set(tags))
            );
        });
  }
}