export interface Tag {
  id:              number;
  name:            string;
  parentId:        number | null;
  children:        Tag[] | null;
  bookmarkIds:     number[];
  backgroundColor: string | null;
  textColor:       string | null;
  createdAt:       string;
  updatedAt:       string;
}

export interface TagPayload {
  name?:            string;
  parentId?:        number;
  backgroundColor?: string;
  textColor?:       string;
}
