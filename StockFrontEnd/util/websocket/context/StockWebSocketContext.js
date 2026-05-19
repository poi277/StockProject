// context/StockWebSocketContext.jsx
'use client';

import { createContext, useContext, useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useAuth } from '../../../context/AuthContext';
import { STOCK_WEBSOCKET_API_URL } from '../../URLconfig';

const StockWebSocketContext = createContext(null);

export function StockWebSocketProvider({ children }) {
  const [stockConnected, setStockConnected] = useState(false);
  const stockClientRef = useRef(null);
  const { user, loading } = useAuth();

  useEffect(() => {
    if (loading) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${STOCK_WEBSOCKET_API_URL}/ws-stock`),
      connectHeaders: user ? { userId: String(user) } : {},
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        console.log('✅ Stock WebSocket 연결 성공!');
        setStockConnected(true);
      },
      onDisconnect: () => setStockConnected(false),
      onWebSocketClose: () => setStockConnected(false),
      onStompError: (frame) => {
        console.error('❌ Stock STOMP 에러:', frame);
        setStockConnected(false);
      },
    });

    stockClientRef.current = client;
    client.activate();

    return () => {
      if (client.active) client.deactivate();
      setStockConnected(false);
    };
  }, [user, loading]);

  return (
    <StockWebSocketContext.Provider value={{
      stockConnected,
      stockClient: stockClientRef.current,
    }}>
      {children}
    </StockWebSocketContext.Provider>
  );
}

export function useStockWebSocket() {
  const context = useContext(StockWebSocketContext);
  if (!context) {
    throw new Error('useStockWebSocket은 StockWebSocketProvider 내부에서만 사용 가능합니다');
  }
  return context;
}