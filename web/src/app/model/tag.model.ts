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

export interface TagNode {
  tag:      Tag;
  children: TagNode[];
  fullPath: string; // "Parent/Child/GrandChild"
}

export interface TagPayload {
  name:      string;
  color?:    string;
  parentId?: number;
}

export function buildTagTree(tags: Tag[], parentPath = ''): TagNode[] {
  return tags.map(tag => {
    const fullPath = parentPath ? `${parentPath} / ${tag.name}` : tag.name;

    return {
      tag,
      fullPath,
      children: tag.children ? buildTagTree(tag.children, fullPath) : [],
    };
  });
}