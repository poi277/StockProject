'use client';

import './MyOrderForm.css';

const statusLabel = {
  PENDING: '대기',
  WAITING: '대기',
  PARTIAL: '부분 체결',
  COMPLETED: '체결 완료',
  CANCELLED: '취소',
};

const formatNumber = (value) => {
  const number = Number(value);
  return Number.isFinite(number) ? number.toLocaleString('ko-KR') : '-';
};

const formatDate = (value) => {
  if (!value) return '-';

  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString('ko-KR');
};

export default function MyOrderForm({ myOrder = [] }) {
  return (
    <main className="my-order-page ho2myi2">
      <section className="my-order-container">
        <header className="my-order-header">
          <h2>주문 목록</h2>
        </header>

        {myOrder.length === 0 ? (
          <p className="my-order-empty">주문 내역이 없습니다.</p>
        ) : (
          <div className="my-order-table-scroll">
            <table className="my-order-table">
              <thead>
                <tr>
                  <th>종목</th>
                  <th>구분</th>
                  <th>주문 가격</th>
                  <th>주문 수량</th>
                  <th>미체결 수량</th>
                  <th>상태</th>
                  <th>주문 시간</th>
                </tr>
              </thead>
              <tbody>
                {myOrder.map((order, index) => {
                  const tradeType = order.tradeType === 'BUY' ? '매수' : '매도';
                  const remainingQuantity = order.remainingQuantity ?? order.leftQuantity ?? order.quantity;

                  return (
                    <tr key={order.id ?? `${order.stockCode}-${index}`}>
                      <td>
                        <div className="my-order-stock">
                          <strong>{order.stockName || order.stockCode || '-'}</strong>
                          {order.stockCode && <span>{order.stockCode}</span>}
                        </div>
                      </td>
                      <td className={order.tradeType === 'BUY' ? 'buy' : 'sell'}>{tradeType}</td>
                      <td>{formatNumber(order.tradePrice)}원</td>
                      <td>{formatNumber(order.quantity)}</td>
                      <td>{formatNumber(remainingQuantity)}</td>
                      <td>
                        <span className="my-order-status">
                          {statusLabel[order.status] ?? order.status ?? '-'}
                        </span>
                      </td>
                      <td className="my-order-date">{formatDate(order.createdAt ?? order.orderTime)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}
