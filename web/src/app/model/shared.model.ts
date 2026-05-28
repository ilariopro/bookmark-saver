export interface Response<T> {
    data: T;
}

export interface PageInfo {
    total:    number;
    size:     number;
    page:     number;
    pages:    number;
    next:     boolean;
    previous: boolean;
}

export interface PagedResponse<T> {
    data: T[],
    meta: PageInfo
}
