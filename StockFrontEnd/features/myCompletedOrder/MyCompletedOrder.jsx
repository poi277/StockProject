'use client';

const statusLabel = {
  COMPLETED: '체결완료',
  CANCELLED: '취소',
};

const statusColor = {
  COMPLETED: '#10b981',
  CANCELLED: '#6b7280',
};

export default function MyCompletedForm({ myCompletedOrder = [] }) {
  return (
    <div style={{ padding: '24px', fontFamily: "'Pretendard', sans-serif" }}>
      <h2 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '16px', color: '#111' }}>
        체결 / 취소 내역
      </h2>

      {myCompletedOrder.length === 0 ? (
        <p style={{ color: '#888', textAlign: 'center', padding: '40px 0' }}>내역이 없습니다.</p>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
          <thead>
            <tr style={{ borderBottom: '2px solid #e5e7eb', color: '#6b7280', textAlign: 'left' }}>
              <th style={th}>종목코드</th>
              <th style={th}>구분</th>
              <th style={th}>체결가</th>
              <th style={th}>체결된 수량</th>
              <th style={th}>상태</th>
              <th style={th}>완료시간</th>
            </tr>
          </thead>
          <tbody>
            {myCompletedOrder.map((order) => (
              <tr key={order.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
                <td style={td}>{order.stockCode}</td>
                <td style={{ ...td, fontWeight: 600, color: order.tradeType === 'BUY' ? '#ef4444' : '#3b82f6' }}>
                  {order.tradeType === 'BUY' ? '매수' : '매도'}
                </td>
                <td style={td}>{order.tradePrice.toLocaleString()}원</td>
                <td style={td}>{order.filledQuantity}</td>
                <td style={td}>
                  <span style={{
                    padding: '2px 8px',
                    borderRadius: '999px',
                    fontSize: '12px',
                    fontWeight: 600,
                    background: statusColor[order.status] + '20',
                    color: statusColor[order.status],
                  }}>
                    {statusLabel[order.status]}
                  </span>
                </td>
                <td style={{ ...td, color: '#9ca3af', fontSize: '12px' }}>
                  {new Date(order.completedAt).toLocaleString('ko-KR')}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

const th = { padding: '8px 12px', fontWeight: 500 };
const td = { padding: '12px 12px', color: '#374151' };