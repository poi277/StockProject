import { useState } from "react";
import { useStockSocket } from "../../../util/useStockSocket";
import { useWebSocket } from "../../../util/WebSocketContext";
import { useStockDetailSocket } from "../../../util/useStockDetailSocket";

export function useStockHeader(initStock) {
  const { connected, client } = useWebSocket();
  //결론 새로운 use stock detail socket을 만들어야함
  const { stock } = useStockDetailSocket(client, connected, initStock);
  return { connected, stock};
}