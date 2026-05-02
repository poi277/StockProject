// useOrderSocket.js
import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getMyAllOrder } from '../../lib/order';

export function useOrderSocket(client, connected) {
  const [orders, setOrders] = useState([]);
  const { user } = useAuth();

  useEffect(() => {
    if (!user) return;
    getOrders();
  }, [user]);

  const getOrders = async () => {
    try {
      const res = await getMyAllOrder();
      if (!res.success) throw new Error(res.message);
      setOrders(res.data);
    } catch (err) {
      console.error('주문 조회 실패:', err.message);
    }
  };

  useEffect(() => {
    if (!client || !connected || !user) return;

    getOrders(); // 재연결 시 재조회

    const sub = client.subscribe('/user/queue/orders', message => {
      const data = JSON.parse(message.body);
      updateOrders(data);
    });

    return () => sub.unsubscribe();
  }, [client, connected, user]);

  const updateOrders = (data) => {
    setOrders(prev => {
      switch (data.status) {
        case 'PENDING': {
          const exists = prev.find(o => o.orderId == data.orderId);
          if (exists) {
            //  이미 있으면 업데이트 (수정된 주문)
            return prev.map(o =>
              o.orderId == data.orderId ? { ...o, ...data } : o
            );
          }
          // 새 주문이면 추가
          return [...prev, data];
        }
        case 'PARTIAL':
          return prev.map(o =>
            o.orderId == data.orderId ? { ...o, ...data } : o
          );
        case 'COMPLETED':
        case 'CANCELLED':
          return prev.filter(o => o.orderId != data.orderId);
        default:
          return prev;
      }
    });
  };

  return { orders, setOrders };
}