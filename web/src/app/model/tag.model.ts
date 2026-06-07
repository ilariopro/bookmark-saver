export interface Tag {
  id:          number;
  name:        string;
  slug:        string;
  bookmarkIds: number[];
  createdAt:   string;
  updatedAt:   string;
}

export interface TagPayload {
  name?: string;
  slug?: string;
}
