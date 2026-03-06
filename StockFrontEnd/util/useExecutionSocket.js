import { useEffect, useState } from 'react';

export function useExecutionSocket(client, connected, stockCode) {
  const [executions, setExecutions] = useState([]);

  useEffect(() => {
    if (!client || !connected || !stockCode) return;

    console.log('체결 내역 구독 시작:', stockCode);

    const subscription = client.subscribe(`/topic/execution/${stockCode}`, message => {
      console.log('RAW 체결 메시지:', message.body);
      const data = JSON.parse(message.body);

      setExecutions(prev => [
        {
          tradeType: data.tradeType,
          price: data.price,
          quantity: data.quantity,
        },
        ...prev.slice(0, 99) 
      ]);
    });

    return () => {
      subscription.unsubscribe();
    };

  }, [client, connected, stockCode]);

  return { executions };
}