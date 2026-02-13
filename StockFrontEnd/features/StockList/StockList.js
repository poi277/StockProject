import { useAuth } from "../../context/AuthContext";
import { useStockSocket } from "../../util/useStockSocket";

export function StockList(stockCode) {

  const { connected, client } = useAuth();
  const { stocks } = useStockSocket(client, connected, stockCode);

  return {
    connected,
    stocks,
  };
}