// useRealTimeTicks.js
import { useState } from 'react'
import { useExecutionSocket } from '../../../../util/websocket/useExecutionSocket';
import { useStockWebSocket } from '../../../../util/websocket/context/StockWebSocketContext';

export function formatTime(timeStr) {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false })
}

export default function useRealTimeTicks(stockCode) {
  const { stockConnected,stockClient  } = useStockWebSocket();
  const { executions } = useExecutionSocket(stockClient, stockConnected, stockCode);
  const [tickType, setTickType] = useState('realtime')
  const isRealtime = tickType === 'realtime'

  return { executions, tickType, setTickType, isRealtime, formatTime }
}