'use client';

import TradeForm from "../Trade/TradeForm";
import { StockList } from "./StockList";

export default function StockListForm({ stocklist }) {
  const stockCodes = stocklist.map(stock => stock.stockCode);

  const { connected, stocks } = StockList(stockCodes);

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

        <div style={{ marginTop: '20px' }}>
          {stocklist.map(stock => {
            const currentStock = stocks[stock.stockCode] || stock;
            
            return (
              <div key={stock.stockCode} style={{
                border: '1px solid #ddd',
                padding: '20px',
                marginBottom: '10px',
                borderRadius: '8px',
                backgroundColor: '#fff',
                cursor: 'pointer',
                transition: 'box-shadow 0.2s'
              }}
              onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)'}
              onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'none'}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <div>
                    <h3>{currentStock.stockName}</h3>
                    <p style={{ color: '#666' }}>{currentStock.stockCode}</p>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    {/* 종가 */}
                    <div style={{ fontSize: '28px', fontWeight: 'bold' }}>
                      {currentStock.closePrice?.toLocaleString() || '0'}원
                    </div>
                    
                    {/* 전일대비 */}
                    <div style={{
                      color: (currentStock.changeAmount || 0) >= 0 ? '#d00' : '#00d',
                      fontWeight: 'bold'
                    }}>
                      {(currentStock.changeAmount || 0) >= 0 ? '▲' : '▼'}
                      {Math.abs(currentStock.changeAmount || 0).toLocaleString()}원
                      ({(currentStock.changeRate || 0).toFixed(2)}%)
                    </div>
                    
                    {/* 시고저 (간단 버전) */}
                    <div style={{ fontSize: '12px', color: '#999', marginTop: '5px' }}>
                      시: {currentStock.openPrice?.toLocaleString() || '0'} | 
                      고: {currentStock.highPrice?.toLocaleString() || '0'} | 
                      저: {currentStock.lowPrice?.toLocaleString() || '0'}
                    </div>
                    
                    {/* 거래량 */}
                    <div style={{ fontSize: '14px', marginTop: '5px' }}>
                      거래량: {(currentStock.volume || 0).toLocaleString()}주
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
      
      <TradeForm />
    </>
  );
}