import { useStockSocket } from "../../util/websocket/useStockSocket";
import { useWebSocket } from "../../util/websocket/context/WebSocketContext";

export function StockList(stockCode,initialStocks) {

  const { connected, client } = useWebSocket();
  const { stocks } = useStockSocket(client, connected, stockCode, initialStocks);

  return {
    connected,
    stocks,
  };
}