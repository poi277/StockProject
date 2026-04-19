// useRealTimeTicks.js
import { useState } from 'react'
import { useWebSocket } from '../../../../util/WebSocket';
import { useExecutionSocket } from '../../../../util/useExecutionSocket';

export function formatTime(timeStr) {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false })
}

export default function useRealTimeTicks(stockCode) {
  const { connected, client } = useWebSocket();
  const { executions } = useExecutionSocket(client, connected, stockCode);
  const [tickType, setTickType] = useState('realtime')
  const isRealtime = tickType === 'realtime'

  return { executions, tickType, setTickType, isRealtime, formatTime }
}