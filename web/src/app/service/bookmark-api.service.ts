// src/app/services/bookmark.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../app.environment';
import { List } from '../model/list.model';
import { Bookmark } from '../model/bookmark.model';
import { Response, PagedResponse } from '../model/shared.model';
import { Tag } from '../model/tag.model';

@Injectable({providedIn: 'root' })
export class BookmarkApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  // ── Bookmarks ─────────────────────────────────────────────────

  public getBookmarks(
    listId: number | null = null,
    tagIds: number[] = [],
    page = 0,
    size = 20,
    sort = 'createdAt,desc'
  ): Observable<PagedResponse<Bookmark>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);

    if (listId !== null) {
      params = params.set('listIds', listId);
    }

    if (tagIds.length) {
      params = params.set('tagIds', tagIds.join(','));
    }

    return this.http.get<PagedResponse<Bookmark>>(`${this.baseUrl}/bookmarks`, { params });
  }

  // ── Lists ─────────────────────────────────────────────────────

  public getLists(): Observable<List[]> {
    return this.http
      .get<Response<List[]>>(`${this.baseUrl}/lists`)
      .pipe(map(response => response.data));
  }

  public createList(payload: Omit<List, 'id'>): Observable<List> {
    return this.http
      .post<Response<List>>(`${this.baseUrl}/lists`, payload)
      .pipe(map(response => response.data));
  }

  public updateList(id: number, payload: Partial<Omit<List, 'id'>>): Observable<List> {
    return this.http
      .patch<Response<List>>(`${this.baseUrl}/lists/${id}`, payload)
      .pipe(map(response => response.data));
  }

  public deleteList(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/lists/${id}`);
  }

  // ── Tags ──────────────────────────────────────────────────────

  public getTags(): Observable<Tag[]> {
    return this.http
      .get<Response<Tag[]>>(`${this.baseUrl}/tags`)
      .pipe(map(response => response.data));
  }

  public deleteTag(name: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/tags/${encodeURIComponent(name)}`);
  }
}