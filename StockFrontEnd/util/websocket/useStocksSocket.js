import { useEffect, useMemo, useState } from 'react';

export function useStocksSocket(stockClient, stockConnected, initialStocks = []) {
  const [stocklist, setStocklist] = useState([]);

  const stocks = useMemo(() => {
    return Array.isArray(initialStocks) ? initialStocks : [];
  }, [initialStocks]);

  const getStockCode = (stock) => {
    return stock?.snapshot?.stockCode ?? stock?.stockCode;
  };

  useEffect(() => {
    setStocklist(stocks);
  }, [stocks]);

  useEffect(() => {
    if (!stockClient || !stockConnected || stocks.length === 0) return;

    const validStocks = stocks.filter(stock => getStockCode(stock));

    const subscriptions = validStocks.map(stock => {
      const stockCode = getStockCode(stock);
      console.log(stockCode);

      return stockClient.subscribe(`/topic/stock/${stockCode}`, message => {
        console.log("데이터 도착");

        const data = JSON.parse(message.body);

        setStocklist(prev =>
          prev.map(item => {
            const itemStockCode = getStockCode(item);

            if (itemStockCode !== data.stockCode) return item;

            if (item.snapshot) {
              return {
                ...item,
                snapshot: {
                  ...item.snapshot,
                  currentPrice: data.currentPrice,
                  changeRate: data.changeRate,
                  value: data.value,
                },
              };
            }

            return {
              ...item,
              currentPrice: data.currentPrice,
              changeRate: data.changeRate,
              value: data.value,
            };
          })
        );
      });
    });

    return () => {
      subscriptions.forEach(sub => sub.unsubscribe());
    };
  }, [stockClient, stockConnected, stocks]);

  return { stocklist };
}