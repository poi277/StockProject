// util/useStockSocket.js
'use client';

import { useEffect, useState } from 'react';
export function useHogaSocket(client, connected, stockCode) {
  const [hogas, setHogas] = useState({});

  useEffect(() => {
    if (!client || !connected || !stockCode) return;

    console.log('호가 구독 시작:', stockCode);

    const sub = client.subscribe(`/topic/hoga/${stockCode}`, message => {
      try {
        const hoga = JSON.parse(message.body);

        setHogas(prev => ({
          ...prev,
          [stockCode]: hoga
        }));

      } catch (error) {
        console.error('호가 파싱 실패:', error);
      }
    });

    return () => {
      console.log('호가 구독 해제:', stockCode);
      sub.unsubscribe();
    };

  }, [client, connected, stockCode]);

  return { hogas };
}