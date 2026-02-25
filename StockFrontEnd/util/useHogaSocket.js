'use client';

import { useEffect, useState } from 'react';

export function useHogaSocket(client, connected, stockCode) {
  const [hogas, setHogas] = useState({
    sellOrders: {},
    buyOrders: {}
  });

  useEffect(() => {
    if (!client || !connected || !stockCode) return;

    console.log('호가 구독 시작:', stockCode);

    const sub = client.subscribe(`/topic/hoga/${stockCode}`, message => {
      try {
        const data = JSON.parse(message.body);
        const { side, price, qty } = data;

        console.log("받은 메시지:", data);

        setHogas(prev => {
          const key = side === 'SELL' ? 'sellOrders' : 'buyOrders';
          const updatedSide = {
            ...prev[key],
            [price]: qty  
          };
          if (qty === 0) {
            delete updatedSide[price];
          }
          return {
            ...prev,
            [key]: updatedSide
          };
        });

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