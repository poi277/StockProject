'use client';

import { useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export const useWebSocket = (url) => {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);

  useEffect(() => {
    const socket = new SockJS(url);
    const client = new Client({
      webSocketFactory: () => socket,
      
      onConnect: () => {
        console.log('✅ WebSocket 연결 성공!');
        setConnected(true);
      },
      
      onDisconnect: () => {
        console.log('❌ WebSocket 연결 끊김');
        setConnected(false);
      },
      
      onStompError: (frame) => {
        console.error('STOMP 에러:', frame);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, [url]);

  const subscribe = (destination, callback) => {
    if (clientRef.current && connected) {
      return clientRef.current.subscribe(destination, callback);
    }
  };

  const publish = (destination, body) => {
    if (clientRef.current && connected) {
      clientRef.current.publish({
        destination,
        body,
      });
    }
  };

  return { connected, subscribe, publish };
};