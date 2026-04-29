export interface List {
    id: number;
    name: string;
    description: string | null;
    bookmarkIds: number[];
    createdAt: string;
    updatedAt: string;
}

export interface ListPayload {
    name?: string;
    description?: string;
}