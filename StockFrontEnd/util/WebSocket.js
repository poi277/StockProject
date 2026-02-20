// context/WebSocketContext.jsx
'use client';

import { createContext, useContext, useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const WebSocketContext = createContext(null);

export function WebSocketProvider({ children }) {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);

  useEffect(() => {
    console.log('🔌 WebSocket 초기화...');

    const client = new Client({
      webSocketFactory: () => {
        console.log('🏭 SockJS 생성');
        return new SockJS('http://localhost:8080/ws');
      },

      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        console.log('✅ WebSocket 연결 성공!');
        setConnected(true);
      },

      onDisconnect: () => {
        console.log('❌ 연결 종료');
        setConnected(false);
      },

      onWebSocketClose: () => {
        console.log('🔌 WebSocket 닫힘');
        setConnected(false);
      },

      onStompError: (frame) => {
        console.error('❌ STOMP 에러:', frame);
        setConnected(false);
      },

      debug: (str) => {
        if (str.includes('ERROR')) {
          console.error('🐛 STOMP:', str);
        }
      }
    });
    clientRef.current = client;
    client.activate();

    return () => {
      console.log('🧹 WebSocket 정리');
      if (client.active) {
        client.deactivate();
      }
    };
  }, []);
  return (
    <WebSocketContext.Provider value={{ connected, client: clientRef.current }}>
      {children}
    </WebSocketContext.Provider>
  );
}

export function useWebSocket() {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket은 WebSocketProvider 내부에서만 사용 가능합니다');
  }
  return context;
}