export interface Response<T> {
    data: T;
}

export interface PagedResponse<T> {
    data: T[],
    meta: {
        total: number;
        size: number;
        page: number;
        pages: number;
        next: boolean;
        previous: boolean;
    }
}

export interface SidebarList {
    id: string;
    name: string;
    description?: string,
    icon: string;
    type: 'default' | 'api'
}