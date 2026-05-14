import { Injectable, inject } from '@angular/core';

import { MatSnackBar } from '@angular/material/snack-bar';

export type NotificationType = 'success' | 'error' | 'info';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly DURATION_MS = 5000;
  
  private readonly snackBar = inject(MatSnackBar);

  public success(message: string): void {
    this.show(message, 'success');
  }

  public error(message: string): void {
    this.show(message, 'error');
  }

  public info(message: string): void {
    this.show(message, 'info');
  }

  private show(message: string, type: NotificationType): void {
    this.snackBar.open(message, '✕', {
      duration:            this.DURATION_MS,
      horizontalPosition:  'right',
      verticalPosition:    'bottom',
      panelClass:          [`notification-${type}`],
    });
  }
}