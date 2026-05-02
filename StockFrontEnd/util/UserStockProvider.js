// context/OrderContext.jsx
'use client';

import { createContext, useContext } from 'react';
import { useWebSocket } from './websocket/context/WebSocketContext';
import { useUserWebSocket } from './websocket/context/UserWebSocketContext'; 
import { useOrderSocket } from './websocket/useOrderSocket';
import { useUserHaveAssetSocket } from './useUserHaveAssetSocket';

const UserContext = createContext(null);

export function UserStockProvider({ children }) {
  const { client, connected } = useWebSocket();
  const { userClient, userConnected } = useUserWebSocket();
  const { orders, setOrders } = useOrderSocket(client, connected);
  const { asset,setAsset,haveStocks, setHaveStocks } = useUserHaveAssetSocket(userClient, userConnected); 

  return (
    <UserContext.Provider value={{
      orders, setOrders,
      asset,setAsset,
      haveStocks, setHaveStocks,
    }}>
      {children}
    </UserContext.Provider>
  );
}

export function useOrder() {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error('useOrder는 OrderProvider 내부에서만 사용 가능합니다');
  }
  return context;
}