
import { useStockDetailSocket } from "../../../util/websocket/useStockDetailSocket";
import { useStockWebSocket } from "../../../util/websocket/context/StockWebSocketContext";

export function useStockHeader(initStock) {
  const { stockClient, stockConnected } = useStockWebSocket();
  const { stock } = useStockDetailSocket(stockClient, stockConnected, initStock);
  return {stock};
}