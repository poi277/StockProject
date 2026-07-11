import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { UserHaveAsset } from '../../lib/user';
import { getStocksByCodesApi } from '../../lib/stock';

export function useUserHaveAssetSocket(userClient, userConnected) {
  const [haveStocks, setHaveStocks] = useState([]);
  const [asset, setAsset] = useState(0);
  const [availableAsset, setAvailableAsset] = useState(0);
  const [initialStocks, setInitialStocks] = useState([]);
  const { user } = useAuth();

  const getHaveAsset = async () => {
    try {
      const res = await UserHaveAsset();
      if (!res.success) throw new Error(res.message);
      setHaveStocks(res.data.haveStocks);
      setAsset(res.data.asset);
      setAvailableAsset(res.data.availableAsset);
    } catch (err) {
      console.error('보유 주식 조회 실패:', err.message);
    }
  };

  useEffect(() => {
    if (haveStocks.length === 0) {
      setInitialStocks([]);
      return;
    }

    const fetchStockInfo = async () => {
      try {
        const stockCodes = haveStocks.map(s => s.stockCode);
        const res = await getStocksByCodesApi(stockCodes);
        const stockArray = Array.isArray(res.data) ? res.data : [];

        setInitialStocks(stockArray);
      } catch (err) {
        console.error('종목 정보 조회 실패:', err.message);
      }
    };

    fetchStockInfo();
  }, [haveStocks]);

  useEffect(() => {
    if (!userClient || !userConnected || !user) return;

    getHaveAsset();
    const subStock = userClient.subscribe('/user/queue/havestock', message => {
      const data = JSON.parse(message.body);
      console.log("havestock 웹소켓 수신", data)
      setHaveStocks(prev => {
        if (data.quantity === 0) {
          return prev.filter(stock => stock.stockCode !== data.stockCode);
        }

        const exists = prev.some(
          stock => stock.stockCode === data.stockCode
        );

        if (exists) {
          return prev.map(stock =>
            stock.stockCode === data.stockCode
              ? { ...stock, ...data }
              : stock
          );
        }

        return [...prev, data];
      });

      if (data.quantity === 0) {
        setInitialStocks(prev =>
          prev.filter(stock => {
            const stockCode =
              stock?.snapshot?.stockCode ?? stock?.stockCode;

            return stockCode !== data.stockCode;
          })
        );
      }
    });

    const subAsset = userClient.subscribe('/user/queue/asset', message => {
      const data = JSON.parse(message.body);
      console.log("asset 웹소켓 수신", data)
      setAsset(data.asset);
      setAvailableAsset(data.availableAsset);
    });

    return () => {
      subStock.unsubscribe();
      subAsset.unsubscribe();
    };
  }, [userClient, userConnected, user]);

  return { haveStocks, setHaveStocks, asset, setAsset, availableAsset, setAvailableAsset, initialStocks };
}