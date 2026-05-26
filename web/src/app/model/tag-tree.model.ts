import { Tag } from "./tag.model";

export interface TagNode {
  tag:      Tag;
  children: TagNode[];
  fullPath: string; // "Parent/Child/GrandChild"
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

export function flattenTagTree(nodes: TagNode[]): { tag: Tag; fullPath: string }[] {
  return nodes.flatMap(node => [
    { tag: node.tag, fullPath: node.fullPath },
    ...flattenTagTree(node.children),
  ]);
}