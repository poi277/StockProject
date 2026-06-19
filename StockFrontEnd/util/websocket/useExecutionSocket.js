import { useEffect, useState } from 'react';

export function useExecutionSocket(client, connected, stockCode) {
  const [executions, setExecutions] = useState([]);

  useEffect(() => {
    if (!client || !connected || !stockCode) return;

    console.log('체결 내역 구독 시작:', stockCode);

    const subscription = client.subscribe(`/topic/execution/${stockCode}`, message => {
      const data = JSON.parse(message.body);
      const execution = {
        tradeType: data.tradeType,
        price: data.price,
        quantity: data.quantity,
        changeRate: data.changeRate,
        totalVolume: data.totalVolume,
        time: data.time,
      };
      setExecutions(prev => [execution, ...prev.slice(0, 99)]);
    });

    return () => {
      subscription.unsubscribe();
    };

  }, [client, connected, stockCode]);

  return { executions };
}