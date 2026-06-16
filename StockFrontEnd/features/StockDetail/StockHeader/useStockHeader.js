import { useStockDetailSocket } from "../../../util/websocket/useStockDetailSocket";
import { useStockWebSocket } from "../../../util/websocket/context/StockWebSocketContext";

export function useStockHeader(initStock) {
  const { stockClient, stockConnected } = useStockWebSocket();
  const { stock } = useStockDetailSocket(stockClient, stockConnected, initStock);

  const formattedStock = stock
    ? {
        ...stock,
        changeRate: Number(stock.changeRate).toFixed(2),
      }
    : stock;

  return { stock: formattedStock };
}