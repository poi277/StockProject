import { createContext, useContext } from 'react';  // useContext 추가
import { useUserWebSocket } from './context/UserWebSocketContext';
import { useWebSocket } from './context/WebSocketContext';
import { useOrderSocket } from './useOrderSocket';
import { useStocksSocket } from './useStocksSocket';
import { useUserHaveAssetSocket } from './useUserHaveAssetSocket';

const UserContext = createContext(null);

export function UserHaveAssetProvider({ children }) {
    const { client, connected } = useWebSocket();
    const { userClient, userConnected } = useUserWebSocket();
    const { orders, setOrders } = useOrderSocket(client, connected);
    const { haveStocks, setHaveStocks, asset, setAsset, availableAsset, setAvailableAsset, initialStocks } = useUserHaveAssetSocket(userClient, userConnected);
    
    const stockCodes = haveStocks?.map(stock => stock.stockCode) ?? [];
    const { stocks } = useStocksSocket(client, connected, stockCodes, initialStocks);

    const stocksArray = Object.values(stocks ?? {}).map(stock => {
        const matched = haveStocks?.find(h => h.stockCode === stock.stockCode);
        const quantity = matched?.quantity ?? 0;
        const avgPrice = matched?.averagePrice ?? 0;
        const diff = (stock.closePrice - avgPrice) * quantity;
        const rate = avgPrice > 0 ? ((diff / (avgPrice * quantity)) * 100).toFixed(2) : 0;
        return { ...stock, quantity, avgPrice, evaluatedAmount: stock.closePrice * quantity, diff, rate };
    });

    const totalDiff = stocksArray.reduce((sum, stock) => sum + stock.diff, 0);
    const totalInvested = haveStocks?.reduce((sum, s) => sum + (s.averagePrice * s.quantity), 0) ?? 0;
    const totalRate = totalInvested > 0 ? ((totalDiff / totalInvested) * 100).toFixed(2) : 0;
    const totalInvestment = totalInvested;

    return (
        <UserContext.Provider value={{
            orders, setOrders,
            asset, setAsset,
            haveStocks, setHaveStocks,
            availableAsset, setAvailableAsset,
            stocksArray, totalDiff, totalRate, totalInvestment
        }}>
            {children}
        </UserContext.Provider>
    );
}

// 추가
export function UserHaveAssetContext() {
    const context = useContext(UserContext);
    if (!context) {
        throw new Error('UserHaveAssetContext는 UserHaveAssetProvider 내부에서만 사용 가능합니다');
    }
    return context;
}