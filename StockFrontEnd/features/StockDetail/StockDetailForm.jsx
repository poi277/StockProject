'use client';

import { StockDetail } from "./StockDetail";
import TradeForm from "../Trade/TradeForm";

export default function StockDetailForm({ stock }) {
  const { connected, stocks } = StockDetail(stock.stockCode);
  
  // WebSocket에서 업데이트된 데이터가 있으면 사용, 없으면 초기 데이터 사용
  const currentStock = stocks[stock.stockCode] || stock;
  
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

        <div style={{ 
          marginTop: '20px', 
          border: '1px solid #ddd', 
          padding: '20px', 
          borderRadius: '8px', 
          backgroundColor: '#fff' 
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <div>
              <h3>{currentStock?.stockName || 'Loading...'}</h3>
              <p>{currentStock?.stockCode || ''}</p>
            </div>
            <div style={{ textAlign: 'right' }}>
              {/* 종가 표시 */}
              <div style={{ fontSize: '28px', fontWeight: 'bold' }}>
                {currentStock?.closePrice?.toLocaleString() || '0'}원
              </div>
              
              {/* 전일대비 */}
              <div style={{ 
                color: (currentStock?.changeAmount || 0) >= 0 ? '#d00' : '#00d', 
                fontWeight: 'bold' 
              }}>
                {(currentStock?.changeAmount || 0) >= 0 ? '▲' : '▼'}
                {Math.abs(currentStock?.changeAmount || 0).toLocaleString()}원
                ({((currentStock?.changeRate || 0).toFixed(2))}%)
              </div>
              
              {/* 시가/고가/저가 추가 (선택사항) */}
              <div style={{ fontSize: '14px', color: '#666', marginTop: '10px' }}>
                <div>시가: {currentStock?.openPrice?.toLocaleString() || '0'}원</div>
                <div>고가: {currentStock?.highPrice?.toLocaleString() || '0'}원</div>
                <div>저가: {currentStock?.lowPrice?.toLocaleString() || '0'}원</div>
              </div>
              
              {/* 거래량 */}
              <div style={{ marginTop: '10px' }}>
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