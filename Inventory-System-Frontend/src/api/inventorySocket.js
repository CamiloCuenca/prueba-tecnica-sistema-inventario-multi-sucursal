// src/services/inventorySocket.js
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';

let client = null;

export function createStompClient({ token, onConnect, onError, debug } = {}) {
  if (client) return client;

  client = new Client({
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    connectHeaders: {
      Authorization: token ? `Bearer ${token}` : ''
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 0,
    heartbeatOutgoing: 20000,
    debug: debug || (() => {})
  });

  client.onConnect = frame => {
    if (onConnect) onConnect(frame);
  };

  client.onStompError = frame => {
    if (onError) onError(frame);
  };

  client.activate();
  return client;
}

export function disconnectStomp() {
  if (client) {
    try { client.deactivate(); } catch (e) {}
    client = null;
  }
}

export function subscribeTopic(topic, handler) {
  if (!client || !client.connected) return null;
  return client.subscribe(topic, msg => {
    try {
      const body = JSON.parse(msg.body);
      handler(body);
    } catch (e) {
      console.error('Malformed WS message', e);
    }
  });
}
