'use client';

import HogaChart from './HogaChart';
import TradeForm from '../Trade/TradeForm';
import { StockDetail } from './useStockDetail';
import useWatch from '../watchList/useWatch';
import ExecutionList from '../execution/ExecutionList';
import CandleForm from '../candle/CandleForm';

export default function StockDetailForm2({ stock, watched }) {
  const { connected, stocks, selectedPrice, setSelectedPrice } =
    StockDetail(stock.stockCode, stock);

  const currentStock = stocks[stock.stockCode] || stock;
  const { isWatched, handleWatchToggle, watchLoading } =
    useWatch(stock.stockCode, watched);

  const openPrice  = currentStock?.openPrice  || 0;
  const closePrice = currentStock?.closePrice || 0;
  const changeAmt  = closePrice - openPrice;
  const changeRate = openPrice ? (changeAmt / openPrice * 100) : 0;
  const isUp       = changeAmt >= 0;
  const fmt        = (n) => Number(n).toLocaleString('ko-KR');

  const HEADER_H = 60;
  const GAP      = 8;
  const PAD      = 8;

  return (
    <div style={{
      width: "100vw",
      minHeight: "100vh",
      background: "#101013",
      color: "#e2e8f0",
      fontFamily: "'Apple SD Gothic Neo', 'Pretendard', sans-serif",
      boxSizing: "border-box",
      margin: 0,
      padding: 0,
    }}>

      {/* ── 상단 헤더 ── */}
      <div style={{
        height: HEADER_H,
        background: "#0e1117",
        borderBottom: "1px solid #1e2535",
        padding: "0 20px",
        display: "flex",
        alignItems: "center",
        gap: 12,
        boxSizing: "border-box",
      }}>
        <div>
          <div style={{ display: "flex", alignItems: "baseline", gap: 8 }}>
            <span style={{ fontSize: 17, fontWeight: 700, color: "#f1f5f9" }}>
              {currentStock?.stockName}
            </span>
            <span style={{ fontSize: 11, color: "#475569" }}>
              {currentStock?.stockCode}
            </span>
          </div>
          <div style={{ display: "flex", alignItems: "baseline", gap: 6, marginTop: 1 }}>
            <span style={{ fontSize: 20, fontWeight: 800, color: isUp ? "#ef5350" : "#3b82f6" }}>
              {fmt(closePrice)}원
            </span>
            <span style={{ fontSize: 11, color: isUp ? "#ef5350" : "#3b82f6" }}>
              {isUp ? "▲" : "▼"} {fmt(Math.abs(changeAmt))}원 ({Math.abs(changeRate).toFixed(2)}%)
            </span>
          </div>
        </div>

        <button
          onClick={handleWatchToggle}
          disabled={watchLoading}
          style={{ background: "none", border: "none", cursor: "pointer", fontSize: 17 }}
        >
          {isWatched ? "❤️" : "🤍"}
        </button>

        <div style={{
          marginLeft: "auto",
          display: "flex", alignItems: "center", gap: 5,
          fontSize: 11,
          color: connected ? "#34d399" : "#ef5350",
        }}>
          <div style={{
            width: 6, height: 6, borderRadius: "50%",
            background: connected ? "#34d399" : "#ef5350",
          }} />
          {connected ? "실시간" : "연결 안됨"}
        </div>
      </div>

      {/* ── 메인 그리드 ── */}
      <div style={{
        display: "grid",
        gridTemplateColumns: "3fr 1fr 1fr",
        gap: GAP,
        padding: PAD,
        height: `calc((100vh - ${HEADER_H}px - ${PAD * 2}px) * 0.8)`,
        boxSizing: "border-box",
      }}>

        {/* ① 차트 + 체결 */}
        <div style={{
          display: "flex", flexDirection: "column",
          gap: GAP, minWidth: 0, overflow: "hidden",
        }}>
          {/* 차트 패널 */}
          <div style={{
            flex: "1 1 0",
            background: "#161b27",
            border: "1px solid #1e2535",
            borderRadius: 10,
            overflow: "hidden",
            minHeight: 0,
          }}>
            <CandleForm
              stockCode={stock.stockCode}
              currentStock={currentStock}
            />
          </div>

          {/* 체결 내역 패널 */}
          <div style={{
            flex: "0 0 180px",
            background: "#161b27",
            border: "1px solid #1e2535",
            borderRadius: 10,
            overflow: "hidden",
          }}>
            <div style={{
              padding: "8px 14px",
              borderBottom: "1px solid #1e2535",
              fontSize: 11, fontWeight: 600, color: "#64748b",
            }}>
              체결 내역
            </div>
            <ExecutionList stockCode={stock.stockCode} />
          </div>
        </div>

        {/* ② 호가 패널 */}
        <div style={{
          background: "#161b27",
          border: "1px solid #1e2535",
          borderRadius: 10,
          overflow: "hidden",
          display: "flex",
          flexDirection: "column",
        }}>
          <HogaChart
            currentStock={currentStock}
            selectedPrice={selectedPrice}
            setSelectedPrice={setSelectedPrice}
          />
        </div>

        {/* ③ 주문 패널 */}
        <div style={{
          background: "#161b27",
          border: "1px solid #1e2535",
          borderRadius: 10,
          overflow: "hidden",
          display: "flex",
          flexDirection: "column",
        }}>
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