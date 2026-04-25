import { Injectable, inject, computed, signal } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { List } from '../model/list.model';
import { Tag } from '../model/tag.model';
import { SidebarList } from '../model/shared.model';

export const DEFAULT_LIST: SidebarList = {
  id: 'all',
  type: 'default',
  name: 'All Bookmarks',
  icon: 'bookmarks'
};

function toSidebarList(list: List): SidebarList {
  return {
    id: String(list.id),
    type: 'api',
    name: list.name,
    icon: 'list',
    // count: list.bookmarkIds.length,
  };
}

@Injectable({ providedIn: 'root' })
export class FilterStateService {
  private readonly router = inject(Router);
  private readonly route  = inject(ActivatedRoute);

  private readonly queryParams = toSignal(this.route.queryParamMap, {
    initialValue: this.route.snapshot.queryParamMap,
  });

  public readonly apiLists = signal<List[]>([]);
  public readonly tags     = signal<Tag[]>([]);

  public readonly allLists = computed<SidebarList[]>(() => [
    DEFAULT_LIST,
    ...this.apiLists().map(toSidebarList),
  ]);

  // ── Selezione corrente ────────────────────────────────────────
  public readonly selectedListKey = computed<string | null>(() =>
    this.queryParams().get('list') ?? 'all'
  );

  public readonly selectedList = computed<SidebarList | undefined>(() =>
    this.allLists().find(list => String(list.id) === this.selectedListKey())
  );

  public readonly selectedTagIds = computed<Set<number>>(() => {
    const tags = this.queryParams().get('tags');

    return tags 
      ? new Set(tags.split(',').map(Number))
      : new Set<number>();
  });

  public readonly selectedTagIdsArray = computed(() => Array.from(this.selectedTagIds()));

  public hasSelectedList(): boolean {
    return this.queryParams().get('list') !== null;
  }

  // ── Actions ───────────────────────────────────────────────────
  public selectList(id: string | number): void {
    this.router.navigate([], {
      queryParams: { list: String(id), tags: null },
      queryParamsHandling: 'merge',
    });
  }

  public setSelectedTags(tagIds: number[]): void {
    this.router.navigate([], {
      queryParams: { tags: tagIds.length ? tagIds.join(',') : null },
      queryParamsHandling: 'merge',
    });
  }
}