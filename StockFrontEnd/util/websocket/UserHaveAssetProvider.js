import { createContext, useContext } from 'react';  // useContext 추가
import { useUserWebSocket } from './context/UserWebSocketContext';
import { useOrderSocket } from './useOrderSocket';
import { useStocksSocket } from './useStocksSocket';
import { useUserHaveAssetSocket } from './useUserHaveAssetSocket';
import { useOrderWebSocket } from './context/OrderWebSocketContext';
import { useStockWebSocket } from './context/StockWebSocketContext';

const UserContext = createContext(null);

export function UserHaveAssetProvider({ children }) {
    //연결은 두개
    //주문용 하나,유저용 하나
    //구독은 여러개
    const { client, connected } = useOrderWebSocket();
    const { userClient, userConnected } = useUserWebSocket();
    const {stockClient,stockConnected} = useStockWebSocket();
    
    const { orders, setOrders,notifications  } = useOrderSocket(client, connected);
    const { haveStocks, setHaveStocks, asset, setAsset, availableAsset, setAvailableAsset, initialStocks } = useUserHaveAssetSocket(userClient, userConnected);
    //havestock->가지고있는 주식 수 만 알려줌
    //stocklist는 가지고있는 주식의 가격(info)를 알려줌
    //가지고 있는 주식 구독
    const { stocklist } = useStocksSocket(stockClient, stockConnected,initialStocks);


    const stocksArray = (Array.isArray(stocklist) ? stocklist : []).map(stock => {
        const matched = haveStocks?.find(h => h.stockCode === stock.stockCode);
        const quantity = matched?.quantity ?? 0;
        const avgPrice = matched?.averagePrice ?? 0;
        const diff = Math.floor((stock.closePrice - avgPrice) * quantity);
        const rate = avgPrice > 0 ? ((diff / (avgPrice * quantity)) * 100).toFixed(2) : 0;
        return { ...stock, quantity, avgPrice, evaluatedAmount: stock.closePrice * quantity, diff, rate };
    });
    // stocksArray 선언 이후로 옮기기

    //손익 합계
    const totalDiff = Math.floor(stocksArray.reduce((sum, stock) => sum + stock.diff, 0));
    const totalInvested = haveStocks?.reduce((sum, s) => sum + (s.averagePrice * s.quantity), 0) ?? 0;
    const totalRate = totalInvested > 0 ? ((totalDiff / totalInvested) * 100).toFixed(2) : 0;
    const totalInvestment = Math.floor(totalInvested + totalDiff);

    return (
        <UserContext.Provider value={{
            orders, setOrders,notifications,
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