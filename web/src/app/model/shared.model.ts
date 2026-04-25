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
    type: 'default' | 'api'
    name: string;
    icon: string;
    count?: number;
}