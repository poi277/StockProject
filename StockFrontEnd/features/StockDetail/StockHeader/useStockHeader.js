import { useState } from "react";
import { useStockSocket } from "../../../util/useStockSocket";
import { useWebSocket } from "../../../util/WebSocket";

export function useStockHeader(stockCode, initialStock) {
  const { connected, client } = useWebSocket();
  //결론 새로운 use stock detail socket을 만들어야함
  const { stocks } = useStockSocket(client, connected, stockCode ? [stockCode] : [], 
    { [stockCode]: initialStock }  // ✅ 초기값으로 stock 넣기
  );
  const [selectedPrice, setSelectedPrice] = useState(null);
  return { connected, stocks, selectedPrice, setSelectedPrice, stockCode};
}