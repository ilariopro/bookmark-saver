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
