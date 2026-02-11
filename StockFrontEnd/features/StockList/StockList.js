import { useStockSocket } from "../../util/useStockSocket";
import { useWebSocket } from "../../util/WebSocket";

export function StockList(stockCodes) {

  const { connected, client } = useWebSocket();
  const { stocks } = useStockSocket(client, connected, stockCodes);
  

  return {
    connected,
    stocks,
  };
}