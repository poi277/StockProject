'use client';

import { useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export function useWebSocket() {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),

      reconnectDelay: 5000,            // 자동 재연결
      heartbeatIncoming: 4000,         // 서버 heartbeat 감지
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log('✅ WebSocket 연결 성공!');
        setConnected(true);
      },

      onDisconnect: () => {
        console.log('❌ 정상 연결 종료');
        setConnected(false);
      },

      onWebSocketClose: () => {
        console.log('🔥 서버 다운 / 강제 종료 감지');
        setConnected(false);
      },

      onStompError: (frame) => {
        console.error('STOMP 에러:', frame);
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
