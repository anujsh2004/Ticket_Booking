import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export class WebSocketClient {
  constructor(showId, onMessageReceived) {
    this.showId = showId;
    this.onMessageReceived = onMessageReceived;
    this.client = null;
    this.subscription = null;
  }

  connect() {
    try {
      this.client = new Client({
        webSocketFactory: () => {
          const backendBase = import.meta.env.VITE_API_URL
            ? import.meta.env.VITE_API_URL.replace(/\/+$/, '')
            : '';
          const wsEndpoint = backendBase ? `${backendBase}/ws` : '/ws';

          try {
            return new SockJS(wsEndpoint);
          } catch (e) {
            const host = backendBase
              ? backendBase.replace(/^https?:\/\//, '')
              : window.location.host;
            const protocol = (backendBase ? backendBase.startsWith('https') : window.location.protocol === 'https:') ? 'wss:' : 'ws:';
            return new WebSocket(`${protocol}//${host}/ws`);
          }
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          const topic = `/topic/shows/${this.showId}/seats`;
          this.subscription = this.client.subscribe(topic, (message) => {
            try {
              const payload = JSON.parse(message.body);
              if (this.onMessageReceived) {
                this.onMessageReceived(payload);
              }
            } catch (e) {
              console.error('Error parsing WebSocket message:', e);
            }
          });
        },
        onStompError: (frame) => {
          console.warn('STOMP broker notice:', frame.headers?.['message']);
        },
      });

      this.client.activate();
    } catch (err) {
      console.warn('WebSocket connection initialization notice:', err);
    }
  }

  disconnect() {
    try {
      if (this.subscription) {
        this.subscription.unsubscribe();
      }
      if (this.client) {
        this.client.deactivate();
      }
    } catch (e) {}
  }
}
