import { Injectable, inject, computed, signal } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map } from 'rxjs';

import { DEFAULT_LISTS, DefaultList, DefaultListId, TagList } from '../model/sidebar.model';
import { buildTagTree, Tag, TagNode } from '../model/tag.model';

export type SelectedList = DefaultList | TagList;

@Injectable({ providedIn: 'root' })
export class FilterStateService {
  private readonly router = inject(Router);
  private readonly route  = inject(ActivatedRoute);

  // ── Remote data ───────────────────────────────────────────────
  readonly tags     = signal<Tag[]>([]);

  readonly tagTree  = computed<TagNode[]>(() => buildTagTree(this.tags()));

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

  // ── Selezione corrente ────────────────────────────────────────
  readonly selectedList = computed<SelectedList | undefined>(() => {
    const path = this.currentPath();

    if (path.startsWith('/favorites')) return DEFAULT_LISTS[1];
    if (path.startsWith('/archived'))  return DEFAULT_LISTS[2];
    if (path.startsWith('/untagged'))  return DEFAULT_LISTS[3];

    if (path.startsWith('/tags/')) {
      const id  = Number(path.split('/tags/')[1]);
      const tag = this.tags().find(t => t.id === id);
      
      if (tag) {
        return {
          id:    tag.id,
          name:  tag.name,
          color: tag.color ?? null,
          icon:  'label',
          type:  'tag',
        }; 
      }
    }

    return DEFAULT_LISTS[0];
  });

  readonly selectedTagIds = computed<Set<number>>(() => {
    const raw = this.queryParams().get('tags');

    return raw
      ? new Set(raw.split(',').map(Number))
      : new Set<number>();
  });

  readonly selectedTagIdsArray = computed(() =>
    Array.from(this.selectedTagIds())
  );

  defaultLists(): DefaultList[] {
    return DEFAULT_LISTS;
  }

  // ── Navigazione ───────────────────────────────────────────────
  selectDefaultList(id: DefaultListId): void {
    const paths: Record<DefaultListId, string> = {
      bookmarks: '/bookmarks',
      favorites: '/favorites',
      archived:  '/archived',
      untagged:  '/untagged',
    };

    this.router.navigate([paths[id]]);
  }

  selectTag(id: number): void {
    this.router.navigate(['tags', id]);
  }

  setSelectedTags(tagIds: number[]): void {
    this.router.navigate([], {
      queryParams:         { tags: tagIds.length ? tagIds.join(',') : null },
      queryParamsHandling: 'merge',
    });
  }
}