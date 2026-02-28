'use client';

import { useEffect, useState } from 'react';

export function useHogaSocket(client, connected, stockCode) {
  const [data, setData] = useState(null);

  useEffect(() => {
    if (!client || !connected || !stockCode) return;

    const sub = client.subscribe(`/topic/hoga/${stockCode}`, message => {
      try {
        const parsed = JSON.parse(message.body);
        //const { side, price, qty } = parsed;
        setData(parsed);
      } catch (error) {
        console.error('호가 파싱 실패:', error);
      }
    });

    return () => sub.unsubscribe();

  }, [client, connected, stockCode]);
  return { data };
}