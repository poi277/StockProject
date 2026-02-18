'use client';

import { useState, useEffect } from 'react';
import styles from '../../css/HogaChart.module.css'
import {getOrdersApi} from '../../lib/trade'

export default function HogaChart({ currentStock, selectedPrice, setSelectedPrice }) {
  const [sellOrders, setSellOrders] = useState([]);
  const [buyOrders, setBuyOrders] = useState([]);

  const basePrice = currentStock?.prevClosePrice || currentStock?.closePrice || 0;
  const closePrice = currentStock?.closePrice || 0;
  const changeAmt = currentStock?.changeAmount || 0;
  const changeRate = currentStock?.changeRate || 0;

  useEffect(() => {
    if (!closePrice || !currentStock?.stockCode) return;

    // 호가 단위 계산
    const unit = closePrice >= 500000 ? 1000
               : closePrice >= 100000 ? 500
               : closePrice >= 50000  ? 100
               : closePrice >= 10000  ? 50
               : 10;

    // 호가 가격 리스트 생성 (현재가 기준 ±5 단계)
    const sellPrices = [4, 3, 2, 1, 0].map(i => closePrice + unit * i);
    const buyPrices = [1, 2, 3, 4].map(i => closePrice - unit * i);

    // ✅ API 호출하여 실제 주문 데이터 가져오기
    getOrdersApi(currentStock.stockCode)
      .then(res => {
        console.log(res);


        const orders = res.data || [];

        const sellDb = orders.filter(o => o.tradeType === "SELL");
        const buyDb  = orders.filter(o => o.tradeType === "BUY");

        const newSell = sellPrices.map(price => {
          const dbOrder = sellDb.find(order => order.tradePrice === price);
          return {
            price,
            qty: dbOrder ? dbOrder.remainingQuantity : 0
          };
        });

        const newBuy = buyPrices.map(price => {
          const dbOrder = buyDb.find(order => order.tradePrice === price);
          return {
            price,
            qty: dbOrder ? dbOrder.remainingQuantity : 0
          };
        });

        setSellOrders(newSell);
        setBuyOrders(newBuy);

      })
      .catch(error => {
        console.error('호가 데이터 로드 실패:', error);
        
        // 에러 시 기본값 (모두 0)
        setSellOrders(sellPrices.map(price => ({ price, qty: 0 })));
        setBuyOrders(buyPrices.map(price => ({ price, qty: 0 })));
      });
  }, [closePrice, currentStock?.stockCode]);

  // 주식 옆에 %비율
  const pct = (price) => {
    if (!basePrice) return '0.00%';
    const v = ((price - basePrice) / basePrice * 100).toFixed(2);
    return (v > 0 ? '+' : '') + v + '%';
  };

  const fmt = (n) => Number(n).toLocaleString('ko-KR');

  const allQtys = [...sellOrders.map(o => o.qty), ...buyOrders.map(o => o.qty)];
  const maxQty = allQtys.length ? Math.max(...allQtys, 1) : 1; // 최소값 1 (0 방지)
  const isUp = changeAmt >= 0;

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

          {/* 매도쪽 */}
          {sellOrders.map(o => {
            const barW = maxQty > 0 ? Math.round((o.qty / maxQty) * 70) : 0;
            return (
              <div key={o.price} className={styles.hogaRow}>
                <div className={`${styles.qtyCell} ${styles.right}`}>
                  <div
                    className={styles.sellBar}
                    style={{ width: barW }}
                  />
                  <span>{o.qty === 0 ? '-' : fmt(o.qty)}</span>
                </div>

                {/* 중앙 가격 */}
                <div
                  className={`${styles.priceCell} ${styles.sell} ${
                    selectedPrice === o.price ? styles.selected : ''
                  }`}
                  onClick={() => setSelectedPrice(o.price)}
                >
                  {fmt(o.price)}
                </div>

                {/* 오른쪽% */}
                <div className={`${styles.pctCell} ${styles.sell}`}>
                  {pct(o.price)}
                </div>
              </div>
            );
          })}

          {/* 매수쪽 */}
          {buyOrders.map(o => {
            const barW = maxQty > 0 ? Math.round((o.qty / maxQty) * 70) : 0;
            return (
              <div key={o.price} className={styles.hogaRow}>
                <div className={`${styles.pctCell} ${styles.buy}`}>
                  {pct(o.price)}
                </div>

                <div
                  className={`${styles.priceCell} ${styles.buy} ${
                    selectedPrice === o.price ? styles.selected : ''
                  }`}
                  onClick={() => setSelectedPrice(o.price)}
                >
                  {fmt(o.price)}
                </div>

                <div className={`${styles.qtyCell} ${styles.left}`}>
                  <div
                    className={styles.buyBar}
                    style={{ width: barW }}
                  />
                  <span>{o.qty === 0 ? '-' : fmt(o.qty)}</span>
                </div>
              </div>
            );
          })}
        </div>

        <div className={styles.hogaInfo}>
          <Info label="시가" value={currentStock?.openPrice} />
          <Info label="고가" value={currentStock?.highPrice} red />
          <Info label="저가" value={currentStock?.lowPrice} />
          <Info label="거래량" value={currentStock?.volume} unit="주" />
        </div>
      </div>
    </div>
  );
}

function Info({ label, value, red, unit="원" }) {
  const fmt = (n) => n ? Number(n).toLocaleString() : '-';

  return (
    <div className={styles.infoRow}>
      <span className={styles.infoLabel}>{label}</span>
      <span
        className={styles.infoVal}
        style={{ color: red ? '#ff3b30' : '#0056e0' }}
      >
        {fmt(value)}{unit}
      </span>
    </div>
  );
}
