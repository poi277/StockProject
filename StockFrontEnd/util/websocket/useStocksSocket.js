import { useEffect, useState } from 'react';
export function useStocksSocket(client, connected, initialStocks = []) {
  const [stocklist, setStocklist] = useState([]);
  // 배열로 정규화
  const stocksArray = Array.isArray(initialStocks)
    ? initialStocks
    : Object.values(initialStocks ?? {});

  useEffect(() => {
    if (stocksArray.length > 0) {
      setStocklist(stocksArray);
    }
  }, [JSON.stringify(stocksArray)]);

  useEffect(() => {
    if (!client || !connected || stocksArray.length === 0) return;

    const validStocks = stocksArray.filter(s => s && typeof s === "object" && s.stockCode);

    const subscriptions = validStocks.map(({ stockCode }) =>
      client.subscribe(`/topic/stock/${stockCode}`, message => {
        const data = JSON.parse(message.body);
        setStocklist(prev =>
          prev.map(stock =>
            stock.stockCode === data.stockCode
              ? { ...stock, closePrice: data.closePrice, changeRate: data.changeRate, value: data.value }
              : stock
          )
        );
      })
    );

    return () => subscriptions.forEach(sub => sub.unsubscribe());
  }, [client, connected, JSON.stringify(stocksArray)]);

  return {  stocklist };
}