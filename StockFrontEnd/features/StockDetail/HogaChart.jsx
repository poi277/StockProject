'use client';

import { useState, useEffect } from 'react';
import styles from '../../css/HogaChart.module.css';
import { getOrderbookApi } from '../../lib/trade';
import { useHogaSocket } from '../../util/useHogaSocket';
import { useWebSocket } from '../../util/WebSocket';
import { useExecutionSocket } from '../../util/useExecutionSocket';

export default function HogaChart({ currentStock, selectedPrice, setSelectedPrice }) {

  const { connected, client } = useWebSocket();
  const [initialSell, setInitialSell] = useState([]);
  const [initialBuy,  setInitialBuy]  = useState([]);

  const openPrice  = currentStock?.openPrice   || 0;
  const closePrice = currentStock?.closePrice  || 0;
  const changeAmt  = currentStock?.changeAmount || 0;
  const changeRate = currentStock?.changeRate   || 0;
  const isUp       = changeAmt >= 0;

  // DB 초기 로딩
useEffect(() => {
  if (!currentStock?.stockCode) return;
  getOrderbookApi(currentStock.stockCode)
    .then(res => {
      const sellDb = res.data?.sellOrders || [];
      const buyDb  = res.data?.buyOrders  || [];
      setInitialSell(sellDb.map(o => ({ price: o.tradePrice, qty: o.remainingQuantity })));
      setInitialBuy(buyDb.map(o => ({ price: o.tradePrice, qty: o.remainingQuantity })));
    })
    .catch(() => { setInitialSell([]); setInitialBuy([]); });
}, [currentStock?.stockCode]);

  const { sellOrders, buyOrders } = useHogaSocket(
    client, connected, currentStock?.stockCode, initialSell, initialBuy
  );
  const { executions } = useExecutionSocket(client, connected, currentStock?.stockCode);

  const priceColor = (price) => {
    if (!openPrice) return '#333';
    return price > openPrice ? '#ff3b30' : price < openPrice ? '#0056e0' : '#333';
  };

  const pct = (price) => {
    if (!openPrice) return '0.00%';
    const v = ((price - openPrice) / openPrice * 100).toFixed(2);
    return (v > 0 ? '+' : '') + v + '%';
  };

  const fmt = (n) => Number(n).toLocaleString('ko-KR');
  const displaySell = [...sellOrders].slice(0, 5).reverse();
  const displayBuy  = [...buyOrders].slice(0, 5);
  const allQtys = [...displaySell.map(o => o.qty), ...displayBuy.map(o => o.qty)];
  const maxQty  = Math.max(...allQtys, 1);

  return (
    <div className={styles.hogaWrap}>

      {/* 헤더 */}
      <div className={styles.hogaHeader}>
        <span className={styles.currentPrice} style={{ color: isUp ? '#ff3b30' : '#0056e0' }}>
          {fmt(closePrice)}
        </span>
        <span className={styles.changeText} style={{ color: isUp ? '#ff3b30' : '#0056e0' }}>
          {isUp ? '▲' : '▼'} {fmt(Math.abs(changeAmt))} ({Math.abs(changeRate).toFixed(2)}%)
        </span>
      </div>

      <div className={styles.hogaBody}>

        {/* ======= 매도 영역: [2fr(1:1) | 1fr] ======= */}
        <div className={styles.sellSection}>

          {/* 왼쪽 2/3: 행 단위로 잔량+바 | 가격 묶기 */}
          <div className={styles.sellLeft}>
            {displaySell.map((o, i) => {
              const barW = Math.round((o.qty / maxQty) * 100) || 0;
              return (
                <div key={`sell-row-${i}`} className={styles.sellRow}>

                  {/* 잔량 + 바 */}
                  <div className={`${styles.cell} ${styles.sellQtyCell}`}>
                    <div className={styles.sellBar} style={{ width: `${barW}%` }} />
                    <span className={styles.sellQtyText}>{fmt(o.qty)}</span>
                  </div>

                  {/* 가격 */}
                  <div
                    className={`${styles.cell} ${styles.priceCell} ${selectedPrice === o.price ? styles.selected : ''}`}
                    style={{ color: priceColor(o.price) }}
                    onClick={() => setSelectedPrice(o.price)}
                  >
                    <span>{fmt(o.price)}</span>
                    <span className={styles.pct}>{pct(o.price)}</span>
                  </div>

                </div>
              );
            })}
          </div>

          {/* 오른쪽 1/3: 거래량 */}
          <div className={styles.col}>
            {displaySell.map((o, i) => (
              <div key={`sell-vol-${i}`} className={styles.cell}>
                {/* 거래량 데이터 */}
              </div>
            ))}
          </div>

        </div>

        {/* 구분선 */}
        <div className={styles.divider} />

        {/* ======= 매수 영역: [1fr | 2fr(1:1)] ======= */}
        <div className={styles.buySection}>

          {/* 왼쪽 1/3: 체결내역 */}
          <div className={styles.col}>
            {executions.slice(0, 5).map((ex, i) => (
              <div key={`ex-${i}`} className={`${styles.cell} ${styles.executionCell}`}>
                <span style={{ color: ex.tradeType === 'BUY' ? '#ff3b30' : '#0056e0' }}>
                  {fmt(ex.price)}
                </span>
                <span style={{ color: ex.tradeType === 'BUY' ? '#ff3b30' : '#0056e0' }}>
                  {fmt(ex.quantity)}
                </span>
              </div>
            ))}
          </div>

          {/* 오른쪽 2/3: 행 단위로 가격 | 잔량+바 묶기 */}
          <div className={styles.buyRight}>
            {displayBuy.map((o, i) => {
              const barW = Math.round((o.qty / maxQty) * 100) || 0;
              return (
                <div key={`buy-row-${i}`} className={styles.buyRow}>

                  {/* 가격 */}
                  <div
                    className={`${styles.cell} ${styles.priceCell} ${selectedPrice === o.price ? styles.selected : ''}`}
                    style={{ color: priceColor(o.price) }}
                    onClick={() => setSelectedPrice(o.price)}
                  >
                    <span>{fmt(o.price)}</span>
                    <span className={styles.pct}>{pct(o.price)}</span>
                  </div>

                  {/* 잔량 + 바 */}
                  <div className={`${styles.cell} ${styles.buyQtyCell}`}>
                    <div className={styles.buyBar} style={{ width: `${barW}%` }} />
                    <span className={styles.buyQtyText}>{fmt(o.qty)}</span>
                  </div>

                </div>
              );
            })}
          </div>

        </div>

      </div>
    </div>
  );
}