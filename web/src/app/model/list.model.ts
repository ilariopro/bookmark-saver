export interface List {
    id: number;
    name: string;
    description?: string;
    bookmarkIds: number[];
    createdAt: string;
    updatedAt: string;
}
