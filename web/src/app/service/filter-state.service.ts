import { Injectable, inject, computed, signal } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map } from 'rxjs';

import { ApiList, DEFAULT_LISTS, DefaultList, DefaultListId } from '../model/sidebar.model';
import { Tag } from '../model/tag.model';

export type SelectedList = DefaultList | ApiList;

@Injectable({ providedIn: 'root' })
export class FilterStateService {
  private readonly router = inject(Router);
  private readonly route  = inject(ActivatedRoute);

  // ── Remote data ───────────────────────────────────────────────
  readonly apiLists = signal<ApiList[]>([]);
  readonly tags     = signal<Tag[]>([]);

  // ── Route e query params come signal ──────────────────────────
  private readonly currentPath = toSignal(
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      map(event => (event as NavigationEnd).urlAfterRedirects.split('?')[0]),
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

    if (path.startsWith('/lists/')) {
      const id = Number(path.split('/lists/')[1]);
      return this.apiLists().find(l => l.id === id);
    }

    return DEFAULT_LISTS[0];
  });

  readonly selectedTagIds = computed<Set<number>>(() => {
    const raw = this.queryParams().get('tags');
    return raw ? new Set(raw.split(',').map(Number)) : new Set<number>();
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
      all:       '/bookmarks',
      favorites: '/favorites',
      archived:  '/archived',
    };
    this.router.navigate([paths[id]]);
  }

  selectApiList(id: number): void {
    this.router.navigate(['/lists', id]);
  }

  setSelectedTags(tagIds: number[]): void {
    this.router.navigate([], {
      queryParams:         { tags: tagIds.length ? tagIds.join(',') : null },
      queryParamsHandling: 'merge',
    });
  }
}