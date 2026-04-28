import { Injectable, OnDestroy, inject, signal } from '@angular/core';
import { MediaMatcher } from '@angular/cdk/layout';

@Injectable({ providedIn: 'root' })
export class ResponsiveStateService implements OnDestroy {
  private readonly mediaQueryList: MediaQueryList;
  private readonly mediaQueryListener: () => void;

  public readonly isMobile = signal(false);

  constructor() {
    const media = inject(MediaMatcher);

    this.mediaQueryList = media.matchMedia('(max-width: 768px)');
    this.isMobile.set(this.mediaQueryList.matches);
    this.mediaQueryListener = () => this.isMobile.set(this.mediaQueryList.matches);
    this.mediaQueryList.addEventListener('change', this.mediaQueryListener);
  }

  public ngOnDestroy(): void {
    this.mediaQueryList.removeEventListener('change', this.mediaQueryListener);
  }
}