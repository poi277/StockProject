'use client';
import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export default function StockListForm() {
  const [connected, setConnected] = useState(false);
  const [stocks, setStocks] = useState({});

  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws');
    const client = new Client({
      webSocketFactory: () => socket,
      
      onConnect: () => {
        console.log('✅ 연결됨!');
        setConnected(true);
        
        // 관심 종목 구독
        const stockCodes = ['005930', '000660', '035420'];
        
        stockCodes.forEach(code => {
          client.subscribe(`/topic/stock/${code}`, (message) => {
            const stock = JSON.parse(message.body);
            setStocks(prev => ({
              ...prev,
              [stock.id]: stock
            }));
          });
        });
      },
      
      onDisconnect: () => {
        console.log('❌ 연결 끊김');
        setConnected(false);
      }
    });

    client.activate();
    return () => client.deactivate();
  }, []);

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

      <div style={{ marginTop: '20px' }}>
        {Object.values(stocks).length === 0 ? (
          <p>데이터를 기다리는 중...</p>
        ) : (
          Object.values(stocks).map(stock => (
            <div key={stock.id} style={{
              border: '1px solid #ddd',
              padding: '20px',
              marginBottom: '10px',
              borderRadius: '8px',
              backgroundColor: '#fff'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <h3 style={{ margin: 0 }}>{stock.name}</h3>
                  <p style={{ margin: 0, color: '#666', fontSize: '14px' }}>{stock.id}</p>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '28px', fontWeight: 'bold' }}>
                    {stock.price?.toLocaleString()}원
                  </div>
                  <div style={{
                    fontSize: '16px',
                    color: stock.changeAmount >= 0 ? '#d00' : '#00d',
                    fontWeight: 'bold'
                  }}>
                    {stock.changeAmount >= 0 ? '▲' : '▼'} 
                    {Math.abs(stock.changeAmount || 0).toLocaleString()}원 
                    ({(stock.changeRate || 0).toFixed(2)}%)
                  </div>
                  <div style={{ fontSize: '14px', color: '#666', marginTop: '5px' }}>
                    거래량: {(stock.volume || 0).toLocaleString()}주
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}