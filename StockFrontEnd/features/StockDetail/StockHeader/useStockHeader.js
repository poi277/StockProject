import { useState } from "react";
import { useStocksSocket } from "../../../util/websocket/useStocksSocket";
import { useWebSocket } from "../../../util/websocket/context/WebSocketContext";
import { useStockDetailSocket } from "../../../util/websocket/useStockDetailSocket";

export function useStockHeader(initStock) {
  const { connected, client } = useWebSocket();
  //결론 새로운 use stock detail socket을 만들어야함
  const { stock } = useStockDetailSocket(client, connected, initStock);
  return { connected, stock};
}