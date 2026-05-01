import { Injectable, signal, ElementRef } from '@angular/core';
import { PagedResponse } from '../model/shared.model';

export type PageLoader<T> = (page: number) => Promise<PagedResponse<T>>;

@Injectable()
export class InfiniteScrollService<T> {
    public readonly total = signal(0);
    public readonly items = signal<T[]>([]);
    public readonly error = signal<string | null>(null);

    public readonly loading     = signal(false);
    public readonly loadingMore = signal(false);
    public readonly hasMore     = signal(true);
    
    private page     = 0;
    private loader?: PageLoader<T>;
    private observer?: IntersectionObserver;

    public setLoader(loader: PageLoader<T>): void {
        this.loader = loader;
    }

    public observeSentinel(sentinel: ElementRef): void {
        this.observer?.disconnect();

        this.observer = new IntersectionObserver(
            entries => {
                if (entries[0].isIntersecting && this.hasMore() && !this.loadingMore() && !this.loading()) {
                this.loadNextPage();
                }
            },
            { threshold: 0.1 }
        );

        this.observer.observe(sentinel.nativeElement);
    }

    public disconnect(): void {
        this.observer?.disconnect();
    }

    public async reset(): Promise<void> {
        this.page = 0;
        this.items.set([]);
        this.hasMore.set(true);
        this.error.set(null);

        await this.loadPage(0, 'initial');
    }

    private async loadNextPage(): Promise<void> {
        this.page += 1;

        await this.loadPage(this.page, 'more');
    }

    private async loadPage(page: number, mode: 'initial' | 'more'): Promise<void> {
        if (!this.loader) return;

        mode === 'initial'
            ? this.loading.set(true)
            : this.loadingMore.set(true);

        try {
            const result = await this.loader(page);

            this.items.update(prev =>
                mode === 'initial'
                    ? result.data
                    : [...prev, ...result.data]
            );

            this.hasMore.set(result.meta.next);
            this.total.set(result.meta.total);
        } catch {
            this.error.set('Could not load bookmarks.');
        } finally {
            mode === 'initial'
                ? this.loading.set(false)
                : this.loadingMore.set(false);
        }
    }
}