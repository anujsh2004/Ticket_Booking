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
    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
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
        console.error('STOMP broker error:', frame.headers['message']);
      },
    });

    this.client.activate();
  }

  disconnect() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    if (this.client) {
      this.client.deactivate();
    }
  }
}
