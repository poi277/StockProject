'use client';

import { useState, useEffect } from 'react';
import styles from '../../css/HogaChart.module.css';
import { getOrdersApi } from '../../lib/trade';
import { useHogaSocket } from '../../util/useHogaSocket';
import { useWebSocket } from '../../util/WebSocket';

function getUnit(price) {
  return price >= 500000 ? 1000
       : price >= 100000 ? 500
       : price >= 50000  ? 100
       : price >= 10000  ? 50
       : 10;
}

function buildLevelsFromBest(bestSell, bestBuy) {
  const sellUnit = getUnit(bestSell);
  const buyUnit  = getUnit(bestBuy);

  const sellPrices = Array.from({ length: 5 }, (_, i) =>
    bestSell + sellUnit * i
  );

  const buyPrices = Array.from({ length: 5 }, (_, i) =>
    bestBuy - buyUnit * i
  );

  return { sellPrices, buyPrices };
}

export default function HogaChart({ currentStock, selectedPrice, setSelectedPrice }) {

  const { connected, client } = useWebSocket();
  const { hogas } = useHogaSocket(client, connected, currentStock?.stockCode);

  const [sellOrders, setSellOrders] = useState([]);
  const [buyOrders, setBuyOrders]   = useState([]);

  const openPrice = currentStock?.openPrice || 0;
  const closePrice = currentStock?.closePrice || 0;
  const changeAmt  = currentStock?.changeAmount || 0;
  const changeRate = currentStock?.changeRate   || 0;
  const isUp       = changeAmt >= 0;

  const priceColor = (price) => {
    if (!openPrice) return '#333';
    return price > openPrice ? '#ff3b30'
         : price < openPrice ? '#0056e0'
         : '#333';
  };

  /* =========================
     ✅ 1. 초기 DB 로딩
  ========================== */
  useEffect(() => {
    if (!currentStock?.stockCode) return;

    getOrdersApi(currentStock.stockCode)
      .then(res => {
        const sellDb = res.data?.sellOrders || [];
        const buyDb  = res.data?.buyOrders  || [];

        const bestSell = sellDb.length > 0
          ? Math.min(...sellDb.map(o => o.tradePrice))
          : closePrice;

        const bestBuy = buyDb.length > 0
          ? Math.max(...buyDb.map(o => o.tradePrice))
          : closePrice;

        const { sellPrices, buyPrices } =
          buildLevelsFromBest(bestSell, bestBuy);

        setSellOrders(
          sellPrices.map(price => ({
            price,
            qty: sellDb
              .filter(o => o.tradePrice === price)
              .reduce((s, o) => s + o.remainingQuantity, 0)
          }))
        );

        setBuyOrders(
          buyPrices.map(price => ({
            price,
            qty: buyDb
              .filter(o => o.tradePrice === price)
              .reduce((s, o) => s + o.remainingQuantity, 0)
          }))
        );
      })
      .catch(() => {
        setSellOrders([]);
        setBuyOrders([]);
      });

  }, [currentStock?.stockCode]);


  /* =========================
     ✅ 2. WebSocket DELTA 반영
  ========================== */
  useEffect(() => {

    setSellOrders(prev =>
      prev.map(order => ({
        ...order,
        qty: hogas.sellOrders?.[order.price] ?? order.qty
      }))
    );

    setBuyOrders(prev =>
      prev.map(order => ({
        ...order,
        qty: hogas.buyOrders?.[order.price] ?? order.qty
      }))
    );

  }, [hogas.sellOrders, hogas.buyOrders]);


  const pct = (price) => {
    if (!openPrice) return '0.00%';
    const v = ((price - openPrice) / openPrice * 100).toFixed(2);
    return (v > 0 ? '+' : '') + v + '%';
  };

  const fmt = (n) => Number(n).toLocaleString('ko-KR');
  const allQtys = [...sellOrders.map(o => o.qty), ...buyOrders.map(o => o.qty)];
  const maxQty  = Math.max(...allQtys, 1);

  return (
    <div className={styles.hogaWrap}>
      <div className={styles.hogaHeader}>
        <span
          className={styles.currentPrice}
          style={{ color: isUp ? '#ff3b30' : '#0056e0' }}
        >
          {fmt(closePrice)}
        </span>
        <span
          className={styles.changeText}
          style={{ color: isUp ? '#ff3b30' : '#0056e0' }}
        >
          {isUp ? '▲' : '▼'} {fmt(Math.abs(changeAmt))} ({Math.abs(changeRate).toFixed(2)}%)
        </span>
      </div>

      <div className={styles.hogaBody}>
        <div className={styles.hogaTable}>

          {/* ===== 매도 ===== */}
          {sellOrders.map(o => {
            const barW = Math.round((o.qty / maxQty) * 70);
            return (
              <div key={o.price} className={styles.hogaRow}>
                <div className={`${styles.qtyCell} ${styles.right}`}>
                  <div className={styles.sellBar} style={{ width: barW }} />
                  <span>{o.qty === 0 ? '-' : fmt(o.qty)}</span>
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

          {/* ===== 매수 ===== */}
          {buyOrders.map(o => {
            const barW = Math.round((o.qty / maxQty) * 70);
            return (
              <div key={o.price} className={styles.hogaRow}>
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
                  <span>{o.qty === 0 ? '-' : fmt(o.qty)}</span>
                </div>
              </div>
            );
          })}

        </div>
      </div>
    </div>
  );
}