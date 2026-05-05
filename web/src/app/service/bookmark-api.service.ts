// src/app/services/bookmark.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../app.environment';
import { List, ListPayload } from '../model/list.model';
import { Bookmark, BookmarkCreatePayload, BookmarkUpdatePayload } from '../model/bookmark.model';
import { Response, PagedResponse } from '../model/shared.model';
import { Tag, TagPayload } from '../model/tag.model';

@Injectable({providedIn: 'root' })
export class BookmarkApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  // ── Bookmarks ─────────────────────────────────────────────────

  public getBookmarks(
    favorites: boolean,
    archived: boolean,
    listId: number | null = null,
    tagIds: number[] = [],
    page = 0,
    size = 24,
    sort = 'createdAt,desc'
  ): Observable<PagedResponse<Bookmark>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);

    if (listId !== null) params = params.set('listId', listId);
    if (favorites)       params = params.set('favorite', true);
    if (archived)        params = params.set('archived', true);
    if (tagIds.length)   params = params.set('tagIds', tagIds.join(','));

    return this.http.get<PagedResponse<Bookmark>>(`${this.baseUrl}/bookmarks`, { params });
  }

  getBookmark(bookmarkId: number): Observable<Bookmark> {
    return this.http
      .get<Response<Bookmark>>(`${this.baseUrl}/bookmarks/${bookmarkId}`)
      .pipe(map(response => response.data));
  }

  public createBookmark(payload: BookmarkCreatePayload): Observable<Bookmark> {
    return this.http
      .post<Response<Bookmark>>(`${this.baseUrl}/bookmarks`, payload)
      .pipe(map(response => response.data));
  }

  public deleteBookmark(bookmarkId: number): Observable<void>  {
    return this.http.delete<void>(`${this.baseUrl}/bookmarks/${bookmarkId}`);
  }

  public updateBookmark(bookmarkId: number, payload: BookmarkUpdatePayload): Observable<Bookmark> {
    return this.http
      .patch<Response<Bookmark>>(`${this.baseUrl}/bookmarks/${bookmarkId}`, payload)
      .pipe(map(response => response.data));
  }

  // ── Lists ─────────────────────────────────────────────────────

  public getLists(): Observable<List[]> {
    const params = new HttpParams().set('sort', 'createdAt,asc');

    return this.http
      .get<Response<List[]>>(`${this.baseUrl}/lists`, { params })
      .pipe(map(response => response.data));
  }

  public createList(payload: ListPayload): Observable<List> {
    return this.http
      .post<Response<List>>(`${this.baseUrl}/lists`, payload)
      .pipe(map(response => response.data));
  }

  public updateList(listId: number, payload: ListPayload): Observable<List> {
    return this.http
      .patch<Response<List>>(`${this.baseUrl}/lists/${listId}`, payload)
      .pipe(map(response => response.data));
  }

  public deleteList(listId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/lists/${listId}`);
  }

  // ── Tags ──────────────────────────────────────────────────────

  public getTags(): Observable<Tag[]> {
    const params = new HttpParams().set('sort', 'name,asc');

    return this.http
      .get<Response<Tag[]>>(`${this.baseUrl}/tags`, { params })
      .pipe(map(response => response.data));
  }

  public createTag(payload: TagPayload): Observable<List> {
    return this.http
      .post<Response<List>>(`${this.baseUrl}/tags`, payload)
      .pipe(map(response => response.data));
  }

  public updateTag(tagId: number, payload: TagPayload): Observable<List> {
    return this.http
      .patch<Response<List>>(`${this.baseUrl}/tags/${tagId}`, payload)
      .pipe(map(response => response.data));
  }

  public deleteTag(tagId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/tags/${tagId}`);
  }
}