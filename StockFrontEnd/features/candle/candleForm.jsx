"use client"

import { useEffect, useRef, useState } from "react";
import useCandle from "./useCandle";
import CandleChart from './candleChart/CandleChart';

const INTERVALS = [
  { label: "1분",  value: "ONE_MINUTE" },
  { label: "3분",  value: "THREE_MINUTE" },
  { label: "5분",  value: "FIVE_MINUTE" },
  { label: "10분", value: "TEN_MINUTE" },
  { label: "15분", value: "FIFTEEN_MINUTE" },
  { label: "30분", value: "THIRTY_MINUTE" },
  { label: "60분", value: "SIXTY_MINUTE" },
];

const INTERVAL_MS = {
  ONE_MINUTE:     60_000,
  THREE_MINUTE:   180_000,
  FIVE_MINUTE:    300_000,
  TEN_MINUTE:     600_000,
  FIFTEEN_MINUTE: 900_000,
  THIRTY_MINUTE:  1_800_000,
  SIXTY_MINUTE:   3_600_000,
};

function getInitialRange() {
  const now   = new Date();
  const start = new Date(now.getTime() - 24 * 60 * 60 * 10000);
  return { start: start.toISOString(), end: now.toISOString() };
}

// ✅ currentStock prop 추가
export default function CandleForm({ stockCode, currentStock }) {
  const [type,       setType]       = useState("ONE_MINUTE");
  const [startTime,  setStartTime]  = useState(() => getInitialRange().start);
  const [endTime,    setEndTime]    = useState(() => getInitialRange().end);
  const [allCandles, setAllCandles] = useState([]);
  const isLoadingMore = useRef(false);
  const isInitialized = useRef(false);

  const { candles } = useCandle(stockCode, type, startTime, endTime);

  // 초기/추가 데이터 로딩
  useEffect(() => {
    if (!candles.length) return;
    setAllCandles(prev => {
      if (!prev.length) {
        isInitialized.current = true;
        return candles;
      }
      const prevTimes = new Set(prev.map(c => c.time));
      const newOnes   = candles.filter(c => !prevTimes.has(c.time));
      if (!newOnes.length) return prev;
      return [...newOnes, ...prev].sort(
        (a, b) => new Date(a.time) - new Date(b.time)
      );
    });
    isLoadingMore.current = false;
  }, [candles]);

  // ✅ 실시간 현재가 → 캔들 업데이트
  useEffect(() => {
    if (!currentStock?.closePrice || !isInitialized.current) return;

    const price     = currentStock.closePrice;
    const tradeTime = currentStock.tradeTime ?? new Date().toISOString();

    const iv       = INTERVAL_MS[type] ?? 60_000;
    const tradeMs  = new Date(tradeTime).getTime();
    const candleMs = Math.floor(tradeMs / iv) * iv;

    setAllCandles(prev => {
      if (!prev.length) return prev;

      const idx = prev.findIndex(
        c => new Date(c.time).getTime() === candleMs
      );

      if (idx === -1) {
        // 새 시간대 → 새 캔들 생성
        const newCandle = {
          time:   new Date(candleMs).toISOString(),
          open:   price,
          high:   price,
          low:    price,
          close:  price,
          volume: 0,
        };
        return [...prev, newCandle].sort(
          (a, b) => new Date(a.time) - new Date(b.time)
        );
      }

      // 기존 캔들 업데이트
      const updated  = prev.slice();
      const c        = { ...updated[idx] };
      c.close        = price;
      c.high         = Math.max(c.high, price);
      c.low          = Math.min(c.low,  price);
      updated[idx]   = c;
      return updated;
    });
  }, [currentStock?.closePrice, currentStock?.tradeTime]);

  const handleEdgeReached = (direction) => {
    if (direction === "left" && !isLoadingMore.current) {
      isLoadingMore.current = true;
      const newEnd   = startTime;
      const newStart = new Date(
        new Date(startTime).getTime() - 24 * 60 * 60 * 1000
      ).toISOString();
      setEndTime(newEnd);
      setStartTime(newStart);
    }
  };

  const handleTypeChange = (newType) => {
    const { start, end } = getInitialRange();
    setType(newType);
    setStartTime(start);
    setEndTime(end);
    setAllCandles([]);
    isInitialized.current = false;
    isLoadingMore.current = false;
  };

  return (
    <div style={{ background: "#131722", borderRadius: 8, overflow: "hidden" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "10px 14px", background: "#1e222d", borderBottom: "0.5px solid #2a2e39" }}>
        <select
          value={type}
          onChange={(e) => handleTypeChange(e.target.value)}
          style={{ background: "#2a2e39", color: "#d1d4dc", border: "0.5px solid #363a45", borderRadius: 4, padding: "4px 8px", fontSize: 12, cursor: "pointer" }}
        >
          {INTERVALS.map((i) => (
            <option key={i.value} value={i.value}>{i.label}</option>
          ))}
        </select>
      </div>

      <CandleChart
        candles={allCandles}
        onEdgeReached={handleEdgeReached}
        intervalType={type}
      />
    </div>
  );
}