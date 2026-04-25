import { TestBed } from '@angular/core/testing';
import { BookmarkApiService } from './bookmark-api.service';

describe('BookmarkServiceService', () => {
  let service: BookmarkApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BookmarkApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
