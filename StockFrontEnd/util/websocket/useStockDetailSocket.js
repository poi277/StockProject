// useStockDetailSocket.js
import { useEffect, useState } from 'react';

export function useStockDetailSocket(client, connected, initStock) {
  const [stock, setStock] = useState(initStock);

  useEffect(() => {
    if (!client || !connected) return;
    if (!initStock) return;

    console.log('주식 구독 시작:', initStock.stockCode);

    const subscription = client.subscribe(`/topic/stock/${initStock.stockCode}`, message => {
      console.log('RAW 메시지:', message.body);
      const data = JSON.parse(message.body);
      setStock(prev => ({
        ...prev,
        closePrice: data.currentPrice,
      }));
    });

    return () => {
      subscription.unsubscribe();
    };

  }, [client, connected, initStock]);

  return { stock };
}