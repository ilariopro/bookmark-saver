import { List } from "./list.model";
import { Tag } from "./tag.model";

export interface Bookmark {
    id: number;
    url: string;
    favorite: boolean;
    notes: string | null;
    lists: List[];
    tags: Tag[];
    metadata: Metadata | null;
    metadataStatus: 'PENDING' | 'SUCCESS' | 'FAILED'
    createdAt: string;
    updatedAt: string;
}

export interface Metadata {
    title: string;
    description: string | null;
    imageUrl: string | null;
    canonicalUrl: string | null;
    siteName: string;
    domain: string;
    favicon: string | null;
    contentType: string;
    extractedAt: string;
}

export interface BookmarkQueryParams {
    favorite: boolean;
    archived: boolean;
    listId: number | null;
    tagIds: number[];
}

export interface BookmarkCreatePayload {
    url: string;
    notes?: string;
    listIds: number[];
    tagIds: number[];
}

export interface BookmarkUpdatePayload {
    favorite?: boolean;
    notes?: string;
    listIds?: number[];
    tagIds?: number[];
}
