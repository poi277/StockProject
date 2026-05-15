
import { useStockDetailSocket } from "../../../util/websocket/useStockDetailSocket";
import { useOrderWebSocket } from "../../../util/websocket/context/OrderWebSocketContext";

export function useStockHeader(initStock) {
  const { connected, client } = useOrderWebSocket();
  //결론 새로운 use stock detail socket을 만들어야함
  const { stock } = useStockDetailSocket(client, connected, initStock);
  return { connected, stock};
}