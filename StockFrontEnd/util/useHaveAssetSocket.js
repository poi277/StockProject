import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { UserHaveStock } from '../lib/user';

export function useHaveAssetSocket(client, connected) {
  const [haveStocks, setHaveStocks] = useState([]);
  const { user } = useAuth();

  useEffect(() => {
    if (!user) return;
    getHaveStocks();
  }, [user]);

  const getHaveStocks = async () => {
    try {
      const res = await UserHaveStock();
      if (!res.success) throw new Error(res.message);
      setHaveStocks(res.data);
    } catch (err) {
      console.error('보유 주식 조회 실패:', err.message);
    }
  };

  useEffect(() => {
    if (!client || !connected || !user) return;

    getHaveStocks(); // 재연결 시 재조회

    const sub = client.subscribe('/user/queue/havestock', message => {
      const data = JSON.parse(message.body);

      setHaveStocks(prev => {
        const exists = prev.find(s => s.stockCode === data.stockCode);
        if (exists) {
          return prev.map(s =>
            s.stockCode === data.stockCode ? { ...s, ...data } : s
          );
        }
        return [...prev, data];
      });
    });

    return () => sub.unsubscribe();
  }, [client, connected, user]);

  return { haveStocks, setHaveStocks };
}