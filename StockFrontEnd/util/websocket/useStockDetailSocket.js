// useStockDetailSocket.js
import { useEffect, useState } from 'react';

export function useStockDetailSocket(client, connected, initStock) {
  const [stock, setStock] = useState(initStock);

  useEffect(() => {
    console.log('client:', client, 'connected:', connected, 'initStock:', initStock);
    if (!client || !connected) return;
    if (!initStock) return;

    console.log('주식 한개 구독 시작:', initStock.stockCode);

    const subscription = client.subscribe(`/topic/stock/${initStock.stockCode}`, message => {
      console.log("주식 한개 메세지 도착")
      const data = JSON.parse(message.body);
      setStock(prev => ({
        ...prev,
        changeRate:data.changeRate,
        changeAmount:data.changeAmount,
        openPrice:data.openPrice,
        currentPrice: data.currentPrice,
        highPrice:data.highPrice,
        lowPrice:data.lowPrice,
      }));
    });

    return () => {
      subscription.unsubscribe();
    };

  }, [client, connected, initStock?.stockCode]);

  return { stock };
}