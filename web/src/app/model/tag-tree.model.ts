import { Tag } from "./tag.model";

export interface TagNode {
  tag:      Tag;
  children: TagNode[];
  fullPath: string; // "Parent / Child / GrandChild"
}

export interface FlattenedTagNode {
  tag:      Tag;
  fullPath: string;
}

export function buildTagPath(name: string, parentPath?: string | null): string {
  const normalized = name
    .trim()
    .toLowerCase()
    .replace(/\s+/g, '-')
    .replace(/[^a-z0-9-_]/g, '');

  return parentPath
    ? `${parentPath}/${normalized}`
    : normalized;
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

export function flattenTagTree(nodes: TagNode[]): FlattenedTagNode[] {
  return nodes.flatMap(node => [
    {
      tag:      node.tag,
      fullPath: node.fullPath
    },
    ...flattenTagTree(node.children),
  ]);
}