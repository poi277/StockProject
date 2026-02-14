// util/useStockSocket.js
'use client';

import { useEffect, useState } from 'react';

export function useStockSocket(client, connected, stockCodes = []) {
  const [stocks, setStocks] = useState({});

  useEffect(() => {
    if (!client || !connected) {
      console.warn(' WebSocket 연결 대기 중...');
      return;
    }

    if (stockCodes.length === 0) {
      console.warn(' 구독할 주식 코드가 없습니다');
      return;
    }

    console.log(' 주식 구독 시작:', stockCodes);

    const subscriptions = [];

    stockCodes.forEach(code => {
      const sub = client.subscribe(`/topic/stock/${code}`, message => {
        try {
          const stock = JSON.parse(message.body);
          console.log(' 가격 업데이트:', stock);
          
          setStocks(prev => ({
            ...prev,
            [stock.id]: stock
          }));
        } catch (error) {
          console.error(' 메시지 파싱 실패:', error);
        }
      });

      subscriptions.push(sub);
      console.log(`${code} 구독 완료`);
    });

    return () => {
      console.log('구독 해제:', stockCodes);
      subscriptions.forEach(sub => sub.unsubscribe());
    };
  }, [client, connected, stockCodes]);

  return { stocks };
}