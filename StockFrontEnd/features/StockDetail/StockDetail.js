import { useStockSocket } from "../../util/useStockSocket";
import { useWebSocket } from "../../util/WebSocket";
import { useState, useEffect } from 'react';

export function StockDetail(stockCodes) {
  const { connected, client } = useWebSocket();
  const { stocks } = useStockSocket(client, connected,[stockCodes]);
  const [selectedPrice, setSelectedPrice] = useState(null);
  return {
    connected,
    stocks,
    setSelectedPrice,
    selectedPrice
  };
}