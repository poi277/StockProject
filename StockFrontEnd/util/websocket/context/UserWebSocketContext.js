// context/UserWebSocketContext.jsx
'use client';

import { createContext, useContext, useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { USER_WEBSOCKET_API_URL } from '../../URLconfig';
import { useAuth } from '../../../context/AuthContext';

const UserWebSocketContext = createContext(null);

export function UserWebSocketProvider({ children }) {
  const [userConnected, setUserConnected] = useState(false);
  const userClientRef = useRef(null);
  const { user, loading } = useAuth();

  useEffect(() => {
    if (loading) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${USER_WEBSOCKET_API_URL}/ws-user`),
      connectHeaders: user ? { userId: String(user) } : {},
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        console.log('✅ User WebSocket 연결 성공!');
        setUserConnected(true);
      },
      onDisconnect: () => setUserConnected(false),
      onWebSocketClose: () => setUserConnected(false),
      onStompError: (frame) => {
        console.error('❌ User STOMP 에러:', frame);
        setUserConnected(false);
      },
    });

    userClientRef.current = client;
    client.activate();

    return () => {
      if (client.active) client.deactivate();
      setUserConnected(false);
    };
  }, [user, loading]);

  return (
    <UserWebSocketContext.Provider value={{
      userConnected,
      userClient: userClientRef.current,
    }}>
      {children}
    </UserWebSocketContext.Provider>
  );
}

export function useUserWebSocket() {
  const context = useContext(UserWebSocketContext);
  if (!context) {
    throw new Error('useUserWebSocket은 UserWebSocketProvider 내부에서만 사용 가능합니다');
  }
  return context;
}