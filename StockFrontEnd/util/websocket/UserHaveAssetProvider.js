// context/OrderContext.jsx
'use client';

import { createContext, useContext } from 'react';
import { useWebSocket } from './context/WebSocketContext';
import { useUserWebSocket } from './context/UserWebSocketContext'; 
import { useOrderSocket } from './useOrderSocket';
import { useUserHaveAssetSocket } from './useUserHaveAssetSocket';

const UserContext = createContext(null);

export function UserHaveAssetProvider({ children }) {
  const { client, connected } = useWebSocket();
  const { userClient, userConnected } = useUserWebSocket();
  const { orders, setOrders } = useOrderSocket(client, connected);
  const { haveStocks, setHaveStocks, asset, setAsset, availableAsset, setAvailableAsset } = useUserHaveAssetSocket(userClient, userConnected); 

  return (
    <UserContext.Provider value={{
      orders, setOrders,
      asset,setAsset,
      haveStocks, setHaveStocks,
      availableAsset, setAvailableAsset
    }}>
      {children}
    </UserContext.Provider>
  );
}

export function UserHaveAssetContext() {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error('useOrder는 OrderProvider 내부에서만 사용 가능합니다');
  }
  return context;
}