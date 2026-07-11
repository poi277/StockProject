// useOrderSocket.js
import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getMyAllOrder } from '../../lib/order';

export function useOrderSocket(client, connected) {
    const [orders, setOrders] = useState([]);
    const [notifications, setNotifications] = useState([]);
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
        getOrders();
        const sub = client.subscribe('/user/queue/orders', message => {
            const data = JSON.parse(message.body);
            updateOrders(data);
        });
        return () => sub.unsubscribe();
    }, [client, connected, user]);

    const updateOrders = (data) => {
        const id = Date.now();
        const notificationText = {
            PENDING: `${data.tradeType === 'BUY' ? '구매' : '판매'} 주문 완료 (${data.stockName})`,

            PARTIAL: `${data.tradeType === 'BUY' ? '구매' : '판매'} 체결 완료 (${data.executedQuantity}주)`,

            COMPLETED: `${data.tradeType === 'BUY' ? '구매' : '판매'} 체결 완료 (${data.executedQuantity}주)`,

            CANCELLED: `${data.tradeType === 'BUY' ? '구매' : '판매'} 주문 취소 (${data.stockName})`,
        }[data.status];

        if (notificationText) {
            setNotifications(prev => [...prev, { ...data, id, text: notificationText }]);
            setTimeout(() => {
                setNotifications(prev => prev.filter(n => n.id !== id));
            }, 3000);
        }

        setOrders(prev => {
            switch (data.status) {
                case 'PENDING': {
                    const exists = prev.find(o => o.orderId == data.orderId);
                    if (exists) {
                        return prev.map(o =>
                            o.orderId == data.orderId ? { ...o, ...data } : o
                        );
                    }
                    return [...prev, data];
                }
                case 'PARTIAL': {
                    const exists = prev.find(o => o.orderId == data.orderId);

                    if (exists) {
                        return prev.map(o =>
                            o.orderId == data.orderId ? { ...o, ...data } : o
                        );
                    }
                    return [...prev, data];
                }
                case 'COMPLETED':
                case 'CANCELLED': {
                    const exists = prev.find(o => o.orderId == data.orderId);
                    if (!exists) return prev;
                    return prev.filter(o => o.orderId != data.orderId);
                }
                default:
                    return prev;
            }
        });
    };

    return { orders, setOrders, notifications };
}