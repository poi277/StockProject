'use client';

import { useEffect, useState } from 'react';

export function useStockSocket(client, connected, stockCodes = []) {
  const [stocks, setStocks] = useState({});

  useEffect(() => {
    if (!client || !connected || stockCodes.length === 0) return;

    const subscriptions = [];

    stockCodes.forEach(code => {
      const sub = client.subscribe(`/topic/stock/${code}`, message => {
        const stock = JSON.parse(message.body);
        setStocks(prev => ({
          ...prev,
          [stock.id]: stock
        }));
      });

      subscriptions.push(sub);
    });

    return () => {
      subscriptions.forEach(sub => sub.unsubscribe());
    };
  }, [client, connected, stockCodes]);

  return { stocks };
}
