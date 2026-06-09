import { Tag } from "./tag.model";

export interface Bookmark {
    id:             number;
    url:            string;
    favorite:       boolean;
    archived:       boolean;
    notes:          string | null;
    tags:           Tag[];
    metadata:       Metadata | null;
    metadataStatus: 'PENDING' | 'SUCCESS' | 'FAILED'
    createdAt:      string;
    updatedAt:      string;
}

export interface Metadata {
    title:        string;
    description:  string | null;
    imageUrl:     string | null;
    canonicalUrl: string | null;
    siteName:     string;
    domain:       string;
    favicon:      string | null;
    contentType:  string;
    extractedAt:  string;
}

export interface BookmarkQueryParams {
    favorite: boolean;
    archived: boolean | null;
    untagged: boolean;
    tagId:    number | null;
    filter:   number[];
}

export interface BookmarkPayload {
    url?:      string;
    favorite?: boolean;
    archived?: boolean;
    notes?:    string;
    tagIds?:   number[];
}

export interface BulkUpdatePayload {
    ids:           number[];
    favorite?:     boolean;
    archived?:     boolean;
    addTagIds?:    number[];
    removeTagIds?: number[];
}

export interface BulkDeletePayload {
    ids: number[];
}