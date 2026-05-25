export type DefaultListId = 'bookmarks' | 'favorites' | 'archived' | 'untagged';

export interface DefaultList {
    id:   DefaultListId;
    name: string;
    icon: string;
    type: 'default';
}

export const DEFAULT_LISTS: DefaultList[] = [
    { id: 'bookmarks', name: 'Bookmarks', icon: 'bookmarks', type: 'default' },
    { id: 'favorites', name: 'Favorites', icon: 'star'     , type: 'default' },
    { id: 'archived',  name: 'Archived',  icon: 'archive'  , type: 'default' },
    { id: 'untagged',  name: 'Untagged',  icon: 'label_off', type: 'default' },
];

export interface TagList {
    id:   number;
    name: string;
    color: string | null;
    icon: 'label';
    type: 'tag';
}