import { useStockSocket } from "../../util/useStockSocket";
import { useWebSocket } from "../../util/WebSocket";

export function StockList(stockCode) {

  const { connected, client } = useWebSocket();
  const { stocks } = useStockSocket(client, connected, stockCode);

  return {
    connected,
    stocks,
  };
}