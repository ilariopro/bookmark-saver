import { List } from "./list.model";
import { Tag } from "./tag.model";

export interface Bookmark {
    id: number;
    url: string;
    notes?: string;
    lists: List[];
    tags: Tag[];
    metadata?: Metadata;
    metadataStatus: 'PENDING' | 'SUCCESS' | 'FAILED'
    createdAt: string;
    updatedAt: string;
}

export interface Metadata {
    title: string;
    description?: string;
    imageUrl?: string;
    canonicalUrl?: string;
    siteName: string;
    domain: string;
    favicon?: string;
    contentType: string;
    extractedAt: string;
}
