import { useAuth } from "../../context/AuthContext";
import { useStockSocket } from "../../util/useStockSocket";

export function StockDetail(stockCodes) {
  const { connected, client } = useAuth();
  const { stocks } = useStockSocket(client, connected,[stockCodes]);

  return {
    connected,
    stocks,
  };
}