'use client';

import { StockDetail } from "./StockDetail";
import TradeForm from "../Trade/TradeForm";

export default function StockDetailForm({ stock }) {
  const { connected, stocks } = StockDetail(stock.id);
  // WebSocket에서 업데이트된 데이터가 있으면 사용, 없으면 초기 데이터 사용
  const currentStock = stocks[stock.id] || stock;
  return (
    <>
      <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
        <h1>📈 실시간 주식 시세</h1>
        <p style={{
          padding: '10px',
          backgroundColor: connected ? '#79b387' : '#f8d7da',
          borderRadius: '5px'
        }}>
          상태: {connected ? '✅ 연결됨' : '❌ 연결 안됨'}
        </p>

        <div style={{ marginTop: '20px', border: '1px solid #ddd', padding: '20px', borderRadius: '8px', backgroundColor: '#fff' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <h3>{currentStock?.name || 'Loading...'}</h3>
              <p>{currentStock?.id || ''}</p>
            </div>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontSize: '28px', fontWeight: 'bold' }}>
                {currentStock?.price?.toLocaleString() || '0'}원
              </div>
              <div style={{ color: (currentStock?.changeAmount || 0) >= 0 ? '#d00' : '#00d', fontWeight: 'bold' }}>
                {(currentStock?.changeAmount || 0) >= 0 ? '▲' : '▼'}
                {Math.abs(currentStock?.changeAmount || 0).toLocaleString()}원
                ({((currentStock?.changeRate || 0).toFixed(2))}%)
              </div>
              <div>
                거래량: {(currentStock?.volume || 0).toLocaleString()}주
              </div>
            </div>
          </div>
        </div>
      </div>
      <TradeForm stock={currentStock} />
    </>
  );
}