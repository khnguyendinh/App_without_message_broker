import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SharedMessage {
  id: string;
  content: string;
  status: string;
  senderApp: string;
  createdAt: string;
  processedAt: string | null;
}

export interface SendBatchResponse {
  sent: number;
  total: number;
}

export interface ReceiverStatus {
  app: string;
  total: number;
  processed: number;
  pending: number;
  failed: number;
}

export interface SenderStatus {
  app: string;
  total: number;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private senderUrl = 'http://localhost:8081/api/messages';
  private receiverUrl = 'http://localhost:8082/api/messages';

  constructor(private http: HttpClient) { }

  // Sender APIs
  sendBatch(count: number, prefix: string): Observable<SendBatchResponse> {
    const params = new HttpParams()
      .set('count', count.toString())
      .set('prefix', prefix);
    return this.http.post<SendBatchResponse>(`${this.senderUrl}/send-batch`, null, { params });
  }

  getSenderMessages(): Observable<SharedMessage[]> {
    return this.http.get<SharedMessage[]>(`${this.senderUrl}/all`);
  }

  getSenderStatus(): Observable<SenderStatus> {
    return this.http.get<SenderStatus>(`${this.senderUrl}/status`);
  }

  // Receiver APIs
  getReceiverMessages(): Observable<SharedMessage[]> {
    return this.http.get<SharedMessage[]>(`${this.receiverUrl}/all`);
  }

  getReceiverStatus(): Observable<ReceiverStatus> {
    return this.http.get<ReceiverStatus>(`${this.receiverUrl}/status`);
  }
}
