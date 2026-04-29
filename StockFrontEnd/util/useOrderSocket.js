// useOrderSocket.js
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getMyAllOrder } from '../lib/order';
import { UserHaveStock } from '../lib/user';

export function useOrderSocket(client, connected) {
  const [orders, setOrders] = useState([]);        // 대기 주문 (PENDING, PARTIAL)
  const [haveStocks, setHaveStocks] = useState([]); // 보유 주식
  const { user } = useAuth();

  // ✅ 초기 데이터 로드
  useEffect(() => {
    if (!user) return;
    getOrders();
    getUserHaveStock();
  }, [user]); // [] → [user] : 로그인 시점에 맞게

  const getOrders = async () => {
    try {
      const res = await getMyAllOrder();
      console.log(res)
      if (!res.success) throw new Error(res.message);
      setOrders(res.data); 
    } catch (err) {
      console.error('주문 조회 실패:', err.message);
    }
  };

  const getUserHaveStock = async () => {
    try {
      const res = await UserHaveStock();
      console.log(res)
      if (!res.success) throw new Error(res.message);
      setHaveStocks(res.data); 
    } catch (err) {
      console.error('주문 조회 실패:', err.message);
    }
  };

  

  // ✅ 웹소켓 실시간 업데이트
  useEffect(() => {
    if (!client || !connected || !user) return;

    const sub = client.subscribe('/user/queue/orders', message => {
      const data = JSON.parse(message.body);

      // 대기 주문 업데이트
      setOrders(prev => {
        switch (data.status) {
          case 'PENDING':
            return [...prev, data];
          case 'PARTIAL':
            return prev.map(o =>
              o.orderId === data.orderId ? { ...o, ...data } : o
            );
          case 'FILLED':
          case 'CANCELLED':
            return prev.filter(o => o.orderId !== data.orderId);
          default:
            return prev;
        }
      });

      // 보유 주식 업데이트 (체결 시)
      if (data.status === 'FILLED' || data.status === 'PARTIAL') {
        setHaveStocks(prev => {
          const exists = prev.find(s => s.stockCode === data.stockCode);
          if (exists) {
            return prev.map(s =>
              s.stockCode === data.stockCode ? { ...s, ...data } : s
            );
          }
          return [...prev, data]; // 새 종목 추가
        });
      }
    });

    return () => sub.unsubscribe();
  }, [client, connected, user]);

  return { orders, setOrders, haveStocks, setHaveStocks };
}