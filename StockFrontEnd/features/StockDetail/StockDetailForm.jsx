'use client';

import { useWebSocket } from "../../util/WebSocket";
import { useStockSocket } from "../../util/useStockSocket";

export default function StockDetailForm({ stockCode }) {
  const { connected, client } = useWebSocket();

  // 🔥 핵심: 문자열 → 배열
  const { stocks } = useStockSocket(client, connected, [stockCode]);

  const stock = stocks[stockCode];

  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
      <h1>📈 실시간 주식 시세</h1>

      <p style={{
        padding: '10px',
        backgroundColor: connected ? '#d4edda' : '#f8d7da',
        borderRadius: '5px'
      }}>
        상태: {connected ? '✅ 연결됨' : '❌ 연결 안됨'}
      </p>

      {!stock ? (
        <p>데이터를 기다리는 중...</p>
      ) : (
        <div style={{
          border: '1px solid #ddd',
          padding: '20px',
          borderRadius: '8px',
          backgroundColor: '#fff'
        }}>
          <h3>{stock.name}</h3>
          <p>{stock.id}</p>

          <div style={{ fontSize: '28px', fontWeight: 'bold' }}>
            {stock.price?.toLocaleString()}원
          </div>

          <div style={{
            color: stock.changeAmount >= 0 ? '#d00' : '#00d',
            fontWeight: 'bold'
          }}>
            {stock.changeAmount >= 0 ? '▲' : '▼'}
            {Math.abs(stock.changeAmount || 0).toLocaleString()}원
            ({(stock.changeRate || 0).toFixed(2)}%)
          </div>

          <div>
            거래량: {(stock.volume || 0).toLocaleString()}주
          </div>
        </div>
      )}
    </div>
  );
}
