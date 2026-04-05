// StockDetailForm.jsx
'use client';

import styles from '../../css/StockDetailForm.module.css';
import HogaChart from './HogaChart';
import TradeForm from '../Trade/TradeForm';
import { StockDetail } from './StockDetail';
import useWatch from '../watchList/useWatch';
import ExecutionList from '../execution/ExecutionList';
import CandleForm from '../candle/CandleForm';

export default function StockDetailForm({ stock, watched }) {
  const { connected, stocks, selectedPrice, setSelectedPrice } = StockDetail(stock.stockCode, stock);
  const currentStock = stocks[stock.stockCode] || stock;
  const { isWatched, handleWatchToggle, watchLoading } = useWatch(stock.stockCode, watched);

  const openPrice  = currentStock?.openPrice  || 0;
  const closePrice = currentStock?.closePrice || 0;
  const changeAmt  = closePrice - openPrice;
  const changeRate = openPrice ? (changeAmt / openPrice * 100) : 0;
  const isUp = changeAmt >= 0;

  return (
    <div style={{ padding: "16px", maxWidth: 1400, margin: "0 auto", fontFamily: "sans-serif" }}>
      {/* 상단: 종목명 + 현재가 + 연결상태 */}
      <div style={{ display: "flex", alignItems: "center", gap: 16, marginBottom: 12 }}>
        <div>
          <h2 style={{ margin: 0 }}>{currentStock?.stockName}</h2>
          <span style={{ fontSize: 13, color: "#888" }}>{currentStock?.stockCode}</span>
        </div>
        <div style={{ marginLeft: "auto", textAlign: "right" }}>
          <div style={{ fontSize: 24, fontWeight: 600, color: isUp ? "#ff3b30" : "#0056e0" }}>
            {closePrice.toLocaleString()}원
          </div>
          <div style={{ fontSize: 13, color: isUp ? "#ff3b30" : "#0056e0" }}>
            {isUp ? "▲" : "▼"} {Math.abs(changeAmt).toLocaleString()}원 ({Math.abs(changeRate).toFixed(2)}%)
          </div>
        </div>
        <button onClick={handleWatchToggle} disabled={watchLoading}
          style={{ background: "none", border: "none", fontSize: 22, cursor: "pointer" }}>
          {isWatched ? "❤️" : "🤍"}
        </button>
        <div style={{
          padding: "4px 10px", borderRadius: 4, fontSize: 12,
          background: connected ? "#79b387" : "#f8d7da",
          color: connected ? "#fff" : "#333"
        }}>
          {connected ? "✅ 연결됨" : "❌ 연결 안됨"}
        </div>
      </div>

      {/* 메인 레이아웃: [차트+체결 | 호가 | 거래] */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 280px 280px", gap: 12, alignItems: "start" }}>
        {/* 1번: 캔들 차트 + 체결내역 */}
        <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
          <CandleForm stockCode={stock.stockCode} />
          <ExecutionList stockCode={stock.stockCode} />
        </div>

        {/* 2번: 호가 */}
        <div>
          <HogaChart
            currentStock={currentStock}
            selectedPrice={selectedPrice}
            setSelectedPrice={setSelectedPrice}
          />
        </div>

        {/* 3번: 거래 */}
        <div>
          <TradeForm
            stockCode={stock.stockCode}
            selectedPrice={selectedPrice}
            setSelectedPrice={setSelectedPrice}
          />
        </div>

      </div>
    </div>
  );
}