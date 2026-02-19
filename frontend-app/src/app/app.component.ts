import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, SharedMessage, ReceiverStatus, SenderStatus } from './services/api.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  // Config
  messageCount = 5;
  messagePrefix = 'Hello Hazelcast';

  // State
  sending = false;
  autoRefresh = true;

  // Data
  senderMessages: SharedMessage[] = [];
  receiverMessages: SharedMessage[] = [];
  senderStatus: SenderStatus | null = null;
  receiverStatus: ReceiverStatus | null = null;

  // Subscriptions
  private pollingSubscription: Subscription | null = null;

  // Logs
  activityLogs: string[] = [];

  constructor(private api: ApiService) { }

  ngOnInit(): void {
    this.refreshData();
    this.startPolling();
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  sendMessages(): void {
    if (this.sending) return;
    this.sending = true;
    this.addLog(`📤 Sending ${this.messageCount} messages with prefix "${this.messagePrefix}"...`);

    this.api.sendBatch(this.messageCount, this.messagePrefix).subscribe({
      next: (res) => {
        this.addLog(`✅ Sent ${res.sent} messages successfully. Total: ${res.total}`);
        this.sending = false;
        this.refreshData();
      },
      error: (err) => {
        this.addLog(`❌ Send failed: ${err.message}`);
        this.sending = false;
      }
    });
  }

  refreshData(): void {
    this.api.getSenderMessages().subscribe({
      next: (msgs) => this.senderMessages = msgs.sort((a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()),
      error: () => { }
    });

    this.api.getReceiverMessages().subscribe({
      next: (msgs) => this.receiverMessages = msgs.sort((a, b) => {
        const dateB = b.processedAt || b.createdAt;
        const dateA = a.processedAt || a.createdAt;
        return new Date(dateB).getTime() - new Date(dateA).getTime();
      }),
      error: () => { }
    });

    this.api.getSenderStatus().subscribe({
      next: (status) => this.senderStatus = status,
      error: () => { }
    });

    this.api.getReceiverStatus().subscribe({
      next: (status) => this.receiverStatus = status,
      error: () => { }
    });
  }

  startPolling(): void {
    this.pollingSubscription = interval(2000).subscribe(() => {
      if (this.autoRefresh) {
        this.refreshData();
      }
    });
  }

  stopPolling(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
      this.pollingSubscription = null;
    }
  }

  toggleAutoRefresh(): void {
    this.autoRefresh = !this.autoRefresh;
    this.addLog(this.autoRefresh ? '🔄 Auto-refresh enabled' : '⏸️ Auto-refresh paused');
  }

  clearLogs(): void {
    this.activityLogs = [];
  }

  getStatusBadge(status: string): string {
    switch (status) {
      case 'PENDING': return 'badge-pending';
      case 'PROCESSING': return 'badge-processing';
      case 'PROCESSED': return 'badge-processed';
      case 'FAILED': return 'badge-failed';
      default: return '';
    }
  }

  formatDate(dateStr: string | null): string {
    if (!dateStr) return '—';
    const date = new Date(dateStr);
    return date.toLocaleTimeString('vi-VN', {
      hour: '2-digit', minute: '2-digit', second: '2-digit', fractionalSecondDigits: 3
    });
  }

  trackById(index: number, item: SharedMessage): string {
    return item.id;
  }

  private addLog(message: string): void {
    const time = new Date().toLocaleTimeString('vi-VN');
    this.activityLogs.unshift(`[${time}] ${message}`);
    if (this.activityLogs.length > 50) {
      this.activityLogs = this.activityLogs.slice(0, 50);
    }
  }
}
