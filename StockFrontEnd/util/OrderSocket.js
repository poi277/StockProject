// context/OrderContext.jsx
'use client';

import { createContext, useContext } from 'react';
import { useWebSocket } from './WebSocketContext';
import { useOrderSocket } from './useOrderSocket';
import { useHaveAssetSocket } from './useHaveAssetSocket';
const OrderContext = createContext(null);

export function OrderProvider({ children }) {
  const { client, connected } = useWebSocket();

  const { orders, setOrders } = useOrderSocket(client, connected);
  const { haveStocks, setHaveStocks } = useHaveAssetSocket(client, connected);

  return (
    <OrderContext.Provider value={{
      orders, setOrders,haveStocks, setHaveStocks
    }}>
      {children}
    </OrderContext.Provider>
  );
}

export function useOrder() {
  const context = useContext(OrderContext);
  if (!context) {
    throw new Error('useOrder는 OrderProvider 내부에서만 사용 가능합니다');
  }
  return context;
}