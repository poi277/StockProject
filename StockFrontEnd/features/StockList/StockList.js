import { useStockSocket } from "../../util/useStockSocket";
import { useWebSocket } from "../../util/WebSocket";

export function StockList(stockCode,initialStocks) {

  const { connected, client } = useWebSocket();
  const { stocks } = useStockSocket(client, connected, stockCode, initialStocks);

  return {
    connected,
    stocks,
  };
}