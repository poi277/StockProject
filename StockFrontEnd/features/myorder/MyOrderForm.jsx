'use client';

import { cancelOrder } from '../../lib/trade';
import { useRouter } from 'next/navigation';

const statusLabel = {
  PENDING: '대기',
  PARTIAL: '부분체결',
  COMPLETED: '체결완료',
  CANCELLED: '취소',
};

const statusColor = {
  PENDING: '#f59e0b',
  PARTIAL: '#3b82f6',
  COMPLETED: '#10b981',
  CANCELLED: '#6b7280',
};

export default function MyOrderForm({ myOrder = [] }) {
  const router = useRouter();

  const handleCancel = async (orderId) => {
    if (!confirm('주문을 취소하시겠습니까?')) return;
    const res = await cancelOrder(orderId);
    if (res.success) {
      router.refresh();
    } else {
      alert(res.message);
    }
  };

  return (
    <div style={{ padding: '24px', fontFamily: "'Pretendard', sans-serif" }}>
      <h2 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '16px', color: '#111' }}>
        나의 주문 내역
      </h2>

      {myOrder.length === 0 ? (
        <p style={{ color: '#888', textAlign: 'center', padding: '40px 0' }}>주문 내역이 없습니다.</p>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
          <thead>
            <tr style={{ borderBottom: '2px solid #e5e7eb', color: '#6b7280', textAlign: 'left' }}>
              <th style={th}>종목명</th>
              <th style={th}>구분</th>
              <th style={th}>주문가</th>
              <th style={th}>수량</th>
              <th style={th}>미체결</th>
              <th style={th}>상태</th>
              <th style={th}>주문시간</th>
              <th style={th}></th>
            </tr>
          </thead>
          <tbody>
            {myOrder.map((order) => (
              <tr key={order.orderId} style={{ borderBottom: '1px solid #f3f4f6' }}>
                <td style={td}>{order.stockName}</td>
                <td style={{ ...td, fontWeight: 600, color: order.tradeType === 'BUY' ? '#ef4444' : '#3b82f6' }}>
                  {order.tradeType === 'BUY' ? '매수' : '매도'}
                </td>
                <td style={td}>{order.tradePrice.toLocaleString()}원</td>
                <td style={td}>{order.quantity}</td>
                <td style={td}>{order.remainingQuantity}</td>
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
                  {new Date(order.createdAt).toLocaleString('ko-KR')}
                </td>
                <td style={td}>
                  {['PENDING', 'PARTIAL'].includes(order.status) && (
                    <button
                      onClick={() => handleCancel(order.orderId)}
                      style={{
                        padding: '4px 10px',
                        fontSize: '12px',
                        borderRadius: '6px',
                        border: '1px solid #ef4444',
                        color: '#ef4444',
                        background: 'white',
                        cursor: 'pointer',
                      }}
                    >
                      취소
                    </button>
                  )}
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