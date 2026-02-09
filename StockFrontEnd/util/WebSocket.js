'use client';

import { useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export function useWebSocket() {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);

  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws');

    const client = new Client({
      webSocketFactory: () => socket,

      onConnect: () => {
        console.log('✅ WebSocket 연결 성공!');
        setConnected(true);
      },

      onDisconnect: () => {
        console.log('❌ WebSocket 연결 끊김');
        setConnected(false);
      }
    });

    clientRef.current = client;
    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);

  return {
    connected,
    client: clientRef.current
  };
}
