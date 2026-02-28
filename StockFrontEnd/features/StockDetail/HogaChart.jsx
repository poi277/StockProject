'use client';

import { useState, useEffect } from 'react';
import styles from '../../css/HogaChart.module.css';
import { getOrdersApi } from '../../lib/trade';
import { useHogaSocket } from '../../util/useHogaSocket';
import { useWebSocket } from '../../util/WebSocket';

export default function HogaChart({ currentStock, selectedPrice, setSelectedPrice }) {

  const { connected, client } = useWebSocket();
  const { data } = useHogaSocket(client, connected, currentStock?.stockCode);

  const [sellOrders, setSellOrders] = useState([]);
  const [buyOrders,  setBuyOrders]  = useState([]);

  const openPrice  = currentStock?.openPrice   || 0;
  const closePrice = currentStock?.closePrice  || 0;
  const changeAmt  = currentStock?.changeAmount || 0;
  const changeRate = currentStock?.changeRate   || 0;
  const isUp       = changeAmt >= 0;

  // 초기 DB 로딩
  useEffect(() => {
    if (!currentStock?.stockCode) return;
    getOrdersApi(currentStock.stockCode)
      .then(res => {
        const sellDb = res.data?.sellOrders || [];
        const buyDb  = res.data?.buyOrders  || [];
        setSellOrders(sellDb
          .map(o => ({ price: o.tradePrice, qty: o.remainingQuantity }))
          .sort((a, b) => a.price - b.price)
        );
        setBuyOrders(buyDb
          .map(o => ({ price: o.tradePrice, qty: o.remainingQuantity }))
          .sort((a, b) => b.price - a.price)
        );
      })
      .catch(() => {
        setSellOrders([]);
        setBuyOrders([]);
      });
  }, [currentStock?.stockCode]);

  // 웹소켓 반영
  useEffect(() => {
    if (!data) return;
    const { side, price, qty } = data;

    if (side === 'SELL') {
      setSellOrders(prev => {
        const exists = prev.some(o => o.price === price);
        if (exists) {
          return prev
            .map(o => o.price === price ? { ...o, qty } : o)
            .filter(o => o.qty > 0);
        }
        if (qty === 0) return prev;
        return [...prev, { price, qty }].sort((a, b) => a.price - b.price);
      });
    } else {
      setBuyOrders(prev => {
        const exists = prev.some(o => o.price === price);
        if (exists) {
          return prev
            .map(o => o.price === price ? { ...o, qty } : o)
            .filter(o => o.qty > 0);
        }
        if (qty === 0) return prev;
        return [...prev, { price, qty }].sort((a, b) => b.price - a.price);
      });
    }
  }, [data]);

  const priceColor = (price) => {
    if (!openPrice) return '#333';
    return price > openPrice ? '#ff3b30'
         : price < openPrice ? '#0056e0'
         : '#333';
  };

  const pct = (price) => {
    if (!openPrice) return '0.00%';
    const v = ((price - openPrice) / openPrice * 100).toFixed(2);
    return (v > 0 ? '+' : '') + v + '%';
  };

  const fmt     = (n) => Number(n).toLocaleString('ko-KR');
  const displaySell = [...sellOrders].slice(0, 5).reverse(); // ✅ slice(-5) → slice(0, 5)
const displayBuy  = [...buyOrders].slice(0, 5);
  const allQtys = [...displaySell.map(o => o.qty), ...displayBuy.map(o => o.qty)];
  const maxQty  = Math.max(...allQtys, 1);

  return (
    <div className={styles.hogaWrap}>
      <div className={styles.hogaHeader}>
        <span className={styles.currentPrice} style={{ color: isUp ? '#ff3b30' : '#0056e0' }}>
          {fmt(closePrice)}
        </span>
        <span className={styles.changeText} style={{ color: isUp ? '#ff3b30' : '#0056e0' }}>
          {isUp ? '▲' : '▼'} {fmt(Math.abs(changeAmt))} ({Math.abs(changeRate).toFixed(2)}%)
        </span>
      </div>

      <div className={styles.hogaBody}>
        <div className={styles.hogaTable}>

          {displaySell.map(o => {
            const barW = Math.round((o.qty / maxQty) * 70) || 0;
            return (
              <div key={`sell-${o.price}`} className={styles.hogaRow}>
                <div className={`${styles.qtyCell} ${styles.right}`}>
                  <div className={styles.sellBar} style={{ width: barW }} />
                  <span>{fmt(o.qty)}</span>
                </div>
                <div
                  className={`${styles.priceCell} ${selectedPrice === o.price ? styles.selected : ''}`}
                  style={{ color: priceColor(o.price) }}
                  onClick={() => setSelectedPrice(o.price)}
                >
                  {fmt(o.price)}
                </div>
                <div className={styles.pctCell} style={{ color: priceColor(o.price) }}>
                  {pct(o.price)}
                </div>
              </div>
            );
          })}

          {displayBuy.map(o => {
            const barW = Math.round((o.qty / maxQty) * 70) || 0;
            return (
              <div key={`buy-${o.price}`} className={styles.hogaRow}>
                <div className={styles.pctCell} style={{ color: priceColor(o.price) }}>
                  {pct(o.price)}
                </div>
                <div
                  className={`${styles.priceCell} ${selectedPrice === o.price ? styles.selected : ''}`}
                  style={{ color: priceColor(o.price) }}
                  onClick={() => setSelectedPrice(o.price)}
                >
                  {fmt(o.price)}
                </div>
                <div className={`${styles.qtyCell} ${styles.left}`}>
                  <div className={styles.buyBar} style={{ width: barW }} />
                  <span>{fmt(o.qty)}</span>
                </div>
              </div>
            );
          })}

        </div>
      </div>
    </div>
  );
}