import { List } from "./list.model";

export type DefaultListId = 'bookmarks' | 'favorites' | 'archived' | 'untagged';

export interface DefaultList {
    id:   DefaultListId;
    name: string;
    description?: string;
    icon: string;
    type: 'default';
}

export const DEFAULT_LISTS: DefaultList[] = [
    { id: 'bookmarks', name: 'Bookmarks', icon: 'bookmarks', type: 'default' },
    { id: 'favorites', name: 'Favorites', icon: 'star'     , type: 'default' },
    { id: 'archived',  name: 'Archived',  icon: 'archive'  , type: 'default' },
    { id: 'untagged',  name: 'Untagged',  icon: 'label_off', type: 'default' },
];

export interface ApiList extends List {
    type: 'api';
    icon: string;
}