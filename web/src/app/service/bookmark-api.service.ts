import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../app.environment';
import { Bookmark, BookmarkPayload, BulkUpdatePayload } from '../model/bookmark.model';
import { Response, PagedResponse } from '../model/shared.model';
import { Tag, TagPayload } from '../model/tag.model';

@Injectable({providedIn: 'root' })
export class BookmarkApiService {
  private readonly BASE_URL = environment.apiBaseUrl;

  private readonly http = inject(HttpClient);

  // ── Bookmarks ─────────────────────────────────────────────────

  public getBookmarks(
    favorite: boolean,
    archived: boolean | null,
    untagged: boolean,
    tagIds:    number[],
    page = 0,
    size = 24,
    sort = 'createdAt,desc'
  ): Observable<PagedResponse<Bookmark>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort)

    if (favorite)          params = params.set('favorite', true);
    if (archived !== null) params = params.set('archived', archived);
    if (untagged)          params = params.set('untagged', true);
    if (tagIds.length)     params = params.set('tagIds',   tagIds.join(','));

    return this.http.get<PagedResponse<Bookmark>>(`${this.BASE_URL}/bookmarks`, { params });
  }

  public getBookmark(bookmarkId: number): Observable<Bookmark> {
    return this.http
      .get<Response<Bookmark>>(`${this.BASE_URL}/bookmarks/${bookmarkId}`)
      .pipe(map(response => response.data));
  }

  public createBookmark(payload: BookmarkPayload): Observable<Bookmark> {
    return this.http
      .post<Response<Bookmark>>(`${this.BASE_URL}/bookmarks`, payload)
      .pipe(map(response => response.data));
  }

  public deleteBookmark(bookmarkId: number): Observable<void>  {
    return this.http.delete<void>(`${this.BASE_URL}/bookmarks/${bookmarkId}`);
  }

  public updateBookmark(bookmarkId: number, payload: BookmarkPayload): Observable<Bookmark> {
    return this.http
      .patch<Response<Bookmark>>(`${this.BASE_URL}/bookmarks/${bookmarkId}`, payload)
      .pipe(map(response => response.data));
  }

  public bulkUpdate(payload: BulkUpdatePayload): Observable<void> {
    return this.http.patch<void>(`${this.BASE_URL}/bookmarks/bulk`, payload);
  }

  public bulkDelete(bookmarkIds: number[]): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/bookmarks/bulk`, {
      body: { ids: bookmarkIds }
    });
  }

  // ── Tags ──────────────────────────────────────────────────────

  public getTags(): Observable<Tag[]> {
    const params = new HttpParams().set('sort', 'name,asc');

    return this.http
      .get<Response<Tag[]>>(`${this.BASE_URL}/tags`, { params })
      .pipe(map(response => response.data));
  }

  public createTag(payload: TagPayload): Observable<Tag> {
    return this.http
      .post<Response<Tag>>(`${this.BASE_URL}/tags`, payload)
      .pipe(map(response => response.data));
  }

  public updateTag(tagId: number, payload: TagPayload): Observable<Tag> {
    return this.http
      .patch<Response<Tag>>(`${this.BASE_URL}/tags/${tagId}`, payload)
      .pipe(map(response => response.data));
  }

  public deleteTag(tagId: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/tags/${tagId}`);
  }
}