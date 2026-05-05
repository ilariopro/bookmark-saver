import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

export type NotificationType = 'success' | 'error' | 'info';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);
  private readonly DURATION_MS = 5000;

  success(message: string): void {
    this.show(message, 'success');
  }

  error(message: string): void {
    this.show(message, 'error');
  }

  info(message: string): void {
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