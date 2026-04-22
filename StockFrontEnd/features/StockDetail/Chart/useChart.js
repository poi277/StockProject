'use client'

import { useEffect, useState } from "react";
import { getCandleApi } from "../../../lib/candle";

export default function useCandle(stockCode, type = "ONE_MINUTE") {
  const [timeRange, setTimeRange] = useState(() => {
    const endTime = new Date();
    const startTime = new Date();
    //원래는 60
    startTime.setMinutes(startTime.getMinutes() - 160);
    return { startTime, endTime };
  });

  const [candles, setCandles] = useState([]);

  const fetchCandle = async (start, end) => {
    try {
      const res = await getCandleApi(stockCode, type, start, end);
      setCandles(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  // 초기 로드만
  useEffect(() => {
    if (!stockCode) return;
    fetchCandle(timeRange.startTime, timeRange.endTime);
  }, [stockCode, type]); // timeRange 의존성 제거

    // 왼쪽 끝 도달 시 추가 로드
    const loadMoreCandles = async () => {
    const newEndTime = timeRange.startTime;
    const newStartTime = new Date(timeRange.startTime);
    newStartTime.setMinutes(newStartTime.getMinutes() - 60);

    try {
      const res = await getCandleApi(stockCode, type, newStartTime, newEndTime);
      if (res.data?.length) {
        setCandles(prev => [...res.data, ...prev]);
        setTimeRange(prev => ({ ...prev, startTime: newStartTime }));
      }
    } catch (err) {
      console.error(err);
    }
  };

  return { candles, loadMoreCandles };
}