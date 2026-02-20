// useStockSocket.js
import { useEffect, useRef, useState } from 'react';

export function useStockSocket(client, connected, stockCodes = []) {
  const [stocks, setStocks] = useState({});
  
  // 문자열로 직렬화해서 실제 값이 바뀔 때만 effect 재실행
  const codesKey = stockCodes.join(',');

  useEffect(() => {
    if (!client || !connected) return;
    if (!stockCodes || stockCodes.length === 0) return;

    const codes = codesKey.split(',').filter(Boolean);
    console.log('주식 구독 시작:', codes);

    const subscriptions = codes.map(code =>
      client.subscribe(`/topic/stock/${code}`, message => {
        const stock = JSON.parse(message.body);
        setStocks(prev => ({
          ...prev,
          [stock.stockCode]: stock
        }));
      })
    );

    return () => {
      subscriptions.forEach(sub => sub.unsubscribe());
    };

  }, [client, connected, codesKey]); // ✅ 배열 대신 문자열 사용

  return { stocks };
}