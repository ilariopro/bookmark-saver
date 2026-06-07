import { Injectable, inject, computed, signal } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map } from 'rxjs';

import { DEFAULT_LISTS, DefaultList, DefaultListId, TagList } from '../model/sidebar.model';
import { Tag } from '../model/tag.model';
import { buildTagTree, FlattenedTagNode, flattenTagTree, TagNode } from '../model/tag-tree.model';

export type SelectedList = DefaultList | TagList;

@Injectable({ providedIn: 'root' })
export class FilterStateService {
  private readonly router = inject(Router);
  private readonly route  = inject(ActivatedRoute);

  // ── Remote data ───────────────────────────────────────────────
  public readonly tags = signal<Tag[]>([]);

  public readonly tagTree          = computed<TagNode[]>(() => buildTagTree(this.tags()));
  public readonly flattenedTagTree = computed<FlattenedTagNode[]>(() => flattenTagTree(this.tagTree()));

  // ── Route e query params come signal ──────────────────────────
  private readonly currentPath = toSignal(
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      map(() => this.router.url.split('?')[0]),
    ),
    { initialValue: this.router.url.split('?')[0] }
  );

  private readonly queryParams = toSignal(this.route.queryParamMap, {
    initialValue: this.route.snapshot.queryParamMap,
  });

  public readonly expandedTagIds = signal<Set<number>>(new Set());

  // ── Selezione corrente ────────────────────────────────────────
  public readonly selectedList = computed<SelectedList | undefined>(() => {
    const path = this.currentPath();

    if (path.startsWith('/favorites')) return DEFAULT_LISTS[1];
    if (path.startsWith('/archived'))  return DEFAULT_LISTS[2];
    if (path.startsWith('/untagged'))  return DEFAULT_LISTS[3];

    if (path.startsWith('/tags/')) {
      const slug = path.split('/tags/')[1];
      const item = this.flattenedTagTree().find(item => item.tag.slug === slug);
      
      if (item) {
        const tag = item.tag;

        return {
          id:              tag.id,
          name:            item.fullPath,
          // name:            tag.name,
          backgroundColor: tag.backgroundColor ?? null,
          textColor:       tag.textColor       ?? null,
          icon:            'label',
          type:            'tag',
        }; 
      }
    }

    return DEFAULT_LISTS[0];
  });

  public readonly selectedTagIds = computed<Set<number>>(() => {
    const raw = this.queryParams().get('filter');

    return raw
      ? new Set(raw.split(',').map(Number))
      : new Set<number>();
  });

  public readonly selectedTagIdsArray = computed(() =>
    Array.from(this.selectedTagIds())
  );

  public defaultLists(): DefaultList[] {
    return DEFAULT_LISTS;
  }

  // ── Navigazione ───────────────────────────────────────────────
  public selectDefaultList(id: DefaultListId): void {
    const paths: Record<DefaultListId, string> = {
      bookmarks: '/bookmarks',
      favorites: '/favorites',
      archived:  '/archived',
      untagged:  '/untagged',
    };

    this.router.navigate([paths[id]]);
  }

  // public selectTag(id: number): void {
  //   this.router.navigate(['tags', id]);
  // }

  public selectTag(slug: string): void {
    this.router.navigate(['tags', slug]);
  }

  public setSelectedTags(tagIds: number[]): void {
    this.router.navigate([], {
      queryParams:         { filter: tagIds.length ? tagIds.join(',') : null },
      queryParamsHandling: 'merge',
    });
  }

  public toggleTagExpansion(id: number): void {
    this.expandedTagIds.update(prev => {
      const next = new Set(prev);

      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }

      return next;
    });
  }
}