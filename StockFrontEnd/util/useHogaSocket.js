'use client';

import { useEffect, useState } from 'react';

export function useHogaSocket(client, connected, stockCode, initialSell = [], initialBuy = []) {
  const [sellOrders, setSellOrders] = useState([]);
  const [buyOrders, setBuyOrders] = useState([]);

  // DB 초기 데이터 반영
  useEffect(() => {
    if (initialSell.length > 0) setSellOrders(initialSell);
  }, [initialSell]);

  useEffect(() => {
    if (initialBuy.length > 0) setBuyOrders(initialBuy);
  }, [initialBuy]);

  // stockCode 바뀌면 초기화
  useEffect(() => {
    setSellOrders([]);
    setBuyOrders([]);
  }, [stockCode]);

  useEffect(() => {
    if (!client || !connected || !stockCode) return;

    console.log('호가 구독 시작:', stockCode);

    const sub = client.subscribe(`/topic/hoga/${stockCode}`, message => {
      try {
        const { side, price, qty } = JSON.parse(message.body);

        if (side === 'SELL') {
          setSellOrders(prev => {
            const exists = prev.some(o => o.price === price);
            if (exists) return prev.map(o => o.price === price ? { ...o, qty } : o).filter(o => o.qty > 0);
            if (qty === 0) return prev;
            return [...prev, { price, qty }].sort((a, b) => a.price - b.price);
          });
        } else {
          setBuyOrders(prev => {
            const exists = prev.some(o => o.price === price);
            if (exists) return prev.map(o => o.price === price ? { ...o, qty } : o).filter(o => o.qty > 0);
            if (qty === 0) return prev;
            return [...prev, { price, qty }].sort((a, b) => b.price - a.price);
          });
        }
      } catch (error) {
        console.error('호가 파싱 실패:', error);
      }
    });

    return () => sub.unsubscribe();

  }, [client, connected, stockCode]);

  return { sellOrders, buyOrders };
}