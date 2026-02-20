import { useMemo, useState } from 'react';
import { useStockSocket } from "../../util/useStockSocket";
import { useWebSocket } from "../../util/WebSocket";

export function StockDetail(stockCode) {
  const { connected, client } = useWebSocket();

  const { stocks } = useStockSocket(client, connected, stockCode ? [stockCode] : []);

  const [selectedPrice, setSelectedPrice] = useState(null);

  return { connected, stocks, selectedPrice, setSelectedPrice };
}