'use client';

import styles from '../../css/StockDetailForm.module.css';
import HogaChart from './HogaChart';
import TradeForm from '../Trade/TradeForm';
import { StockDetail } from './StockDetail';

export default function StockDetailForm({ stock }) {
  const { connected, stocks,selectedPrice,setSelectedPrice } = StockDetail(stock.stockCode);
  const currentStock = stocks[stock.stockCode] || stock;

  const changeAmt  = currentStock?.changeAmount || 0;
  const changeRate = currentStock?.changeRate || 0;
  const isUp = changeAmt >= 0;

  return (
    <>
      <div className={styles.container}>
        <h1>📈 실시간 주식 시세</h1>

        <p
          className={styles.status}
          style={{ backgroundColor: connected ? '#79b387' : '#f8d7da' }}
        >
          상태: {connected ? '✅ 연결됨' : '❌ 연결 안됨'}
        </p>

        <div className={styles.card}>
          <div className={styles.headerRow}>
            <div>
              <h3>{currentStock?.stockName}</h3>
              <p>{currentStock?.stockCode}</p>
            </div>

            <div className={styles.priceBox}>
              <div className={styles.price}>
                {currentStock?.closePrice?.toLocaleString()}원
              </div>

              <div
                className={styles.change}
                style={{ color: isUp ? '#ff3b30' : '#0056e0' }}
              >
                {isUp ? '▲' : '▼'}
                {Math.abs(changeAmt).toLocaleString()}원
                ({Math.abs(changeRate).toFixed(2)}%)
              </div>
            </div>
          </div>
        </div>

       <HogaChart
          currentStock={currentStock}
          selectedPrice={selectedPrice}
          setSelectedPrice={setSelectedPrice}
        />
      </div>

      <TradeForm stockCode={stock.stockCode} selectedPrice={selectedPrice}/>
    </>
  );
}
