import { List } from "./list.model";

export type DefaultListId = 'all' | 'favorites' | 'archived';

export interface DefaultList {
    id:   DefaultListId;
    name: string;
    description?: string;
    icon: string;
    type: 'default';
}

export const DEFAULT_LISTS: DefaultList[] = [
    { id: 'all',       name: 'All Bookmarks', icon: 'bookmarks', type: 'default' },
    { id: 'favorites', name: 'Favorites',     icon: 'star'     , type: 'default' },
    { id: 'archived',  name: 'Archived',      icon: 'archive'  , type: 'default' },
];

export interface ApiList extends List {
    type: 'api';
    icon: string;
}