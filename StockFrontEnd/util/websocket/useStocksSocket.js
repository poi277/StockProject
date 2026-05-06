// useStockSocket.js
import { useEffect, useRef, useState } from 'react';

export function useStocksSocket(client, connected, stockCodes = [], initialStocks = {}) {
  const [stocks, setStocks] = useState({});

  const codesKey = stockCodes.join(',');
  
  useEffect(() => {
    if (Object.keys(initialStocks).length > 0) {
      setStocks(initialStocks);
    }
  }, [JSON.stringify(initialStocks)]); 


  useEffect(() => {
    if (!client || !connected) return;
    if (!stockCodes || stockCodes.length === 0) return;

    const codes = codesKey.split(',').filter(Boolean);
    console.log('주식 여러개  구독 시작:', codes);

    const subscriptions = codes.map(code =>
      client.subscribe(`/topic/stock/${code}`, message => {
        console.log('RAW 메시지:', message.body);
        const data = JSON.parse(message.body);
        setStocks(prev => ({
          ...prev,
          [data.stockCode]: {
            ...prev[data.stockCode],  //  기존 데이터 유지
            closePrice: data.currentPrice,  // 현재가만 업데이트
          }
        }));
      })
    );

    return () => {
      subscriptions.forEach(sub => sub.unsubscribe());
    };

  }, [client, connected, codesKey]); //  배열 대신 문자열 사용

  return { stocks };
}