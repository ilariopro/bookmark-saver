export interface Tag {
  id:          number;
  name:        string;
  color:       string | null;
  parentId:    number | null;
  children:    Tag[] | null;
  bookmarkIds: number[];
  createdAt:   string;
  updatedAt:   string;
}

export interface TagPayload {
  name:      string;
  color?:    string;
  parentId?: number;
}
