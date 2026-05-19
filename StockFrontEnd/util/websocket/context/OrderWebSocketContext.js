// context/WebSocketContext.jsx
'use client';

import { createContext, useContext, useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { WEBSOCKET_API_URL } from '../../URLconfig';
import { useAuth } from '../../../context/AuthContext';

const WebSocketContext = createContext(null);

export function OrderWebSocketProvider({ children }) {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);
  const { user, loading } = useAuth();

  useEffect(() => {
    if (loading) return; // ✅ 세션 확인 전 대기

    const client = new Client({
      webSocketFactory: () => new SockJS(`${WEBSOCKET_API_URL}/ws-order`),

      connectHeaders: user ? { userId: String(user) } : {},

      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        console.log('✅ OrderWebSocket 연결 성공!', 'userId:', user?.userId);
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
        if (str.includes('ERROR')) console.error('🐛 STOMP:', str);
      }
    });

    clientRef.current = client;
    client.activate();

    return () => {
      if (client.active) client.deactivate();
      setConnected(false);
    };
  }, [user, loading]); // ✅ user 바뀔 때 재연결

  return (
    <WebSocketContext.Provider value={{ connected, client: clientRef.current }}>
      {children}
    </WebSocketContext.Provider>
  );
}

export function useOrderWebSocket() {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket은 WebSocketProvider 내부에서만 사용 가능합니다');
  }
  return context;
}