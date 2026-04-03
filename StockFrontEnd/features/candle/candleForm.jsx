// CandleForm.jsx
import { useState, useRef } from "react";
import useCandle from "./useCandle";
import CandleChart from "./CandleChart";

const INTERVALS = [
  { label: "1분",  value: "ONE_MINUTE" },
  { label: "3분",  value: "THREE_MINUTE" },
  { label: "5분",  value: "FIVE_MINUTE" },
  { label: "10분", value: "TEN_MINUTE" },
  { label: "15분", value: "FIFTEEN_MINUTE" },
  { label: "30분", value: "THIRTY_MINUTE" },
  { label: "60분", value: "SIXTY_MINUTE" },
];

function getInitialRange() {
  const now   = new Date();
  const start = new Date(now.getTime() - 24 * 60 * 60 * 1000);
  return { start: start.toISOString(), end: now.toISOString() };
}

export default function CandleForm({ stockCode }) {
  const [type,      setType]      = useState("ONE_MINUTE");
  const [startTime, setStartTime] = useState(() => getInitialRange().start);
  const [endTime,   setEndTime]   = useState(() => getInitialRange().end);

  // ✅ 문제1: 기존 데이터 유지하며 앞에 합치기
  const [allCandles, setAllCandles] = useState([]);
  const isLoadingMore = useRef(false);

  const { candles } = useCandle(stockCode, type, startTime, endTime);

  // candles가 새로 오면 기존 데이터 앞에 합치기
  useState(() => {
    if (!candles.length) return;
    setAllCandles(prev => {
      if (!prev.length) return candles;
      // 중복 제거 후 합치기 (시간 기준)
      const prevTimes = new Set(prev.map(c => c.time));
      const newOnes   = candles.filter(c => !prevTimes.has(c.time));
      if (!newOnes.length) return prev;
      return [...newOnes, ...prev].sort(
        (a, b) => new Date(a.time) - new Date(b.time)
      );
    });
    isLoadingMore.current = false;
  }, [candles]);

  // ✅ useEffect로 교체
  const { useEffect } = require("react");
  useEffect(() => {
    if (!candles.length) return;
    setAllCandles(prev => {
      if (!prev.length) return candles;
      const prevTimes = new Set(prev.map(c => c.time));
      const newOnes   = candles.filter(c => !prevTimes.has(c.time));
      if (!newOnes.length) return prev;
      return [...newOnes, ...prev].sort(
        (a, b) => new Date(a.time) - new Date(b.time)
      );
    });
    isLoadingMore.current = false;
  }, [candles]);

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
    setAllCandles([]); // 분 단위 바뀌면 초기화
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