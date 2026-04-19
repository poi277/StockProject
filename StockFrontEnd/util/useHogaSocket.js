'use client';

import { useEffect, useState } from 'react';

export function useHogaSocket(client, connected, stockCode, { onSellUpdate, onBuyUpdate }) {
  useEffect(() => {
    if (!client || !connected || !stockCode) return;

    const sub = client.subscribe(`/topic/hoga/${stockCode}`, message => {
      try {
        const { side, price, qty } = JSON.parse(message.body);
        if (side === 'SELL') onSellUpdate({ price, qty });
        else onBuyUpdate({ price, qty });
      } catch (error) {
        console.error('호가 파싱 실패:', error);
      }
    });

    return () => sub.unsubscribe();
  }, [client, connected, stockCode]);
}