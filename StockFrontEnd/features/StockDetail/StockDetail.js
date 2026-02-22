import { useMemo, useState } from 'react';
import { useStockSocket } from "../../util/useStockSocket";
import { useWebSocket } from "../../util/WebSocket";

export function StockDetail(stockCode, initialStock) {
  const { connected, client } = useWebSocket();
  const { stocks } = useStockSocket(client, connected, stockCode ? [stockCode] : [], 
    { [stockCode]: initialStock }  // ✅ 초기값으로 stock 넣기
  );

  const [selectedPrice, setSelectedPrice] = useState(null);
  return { connected, stocks, selectedPrice, setSelectedPrice };
}