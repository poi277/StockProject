'use client'

import { useEffect, useState } from "react";
import { getCandleApi } from "../../lib/candle";

export default function useCandle(stockCode, type = "ONE_MINUTE", startTime, endTime) {
  const [candles, setCandles] = useState([]);

  const fetchCandle = async () => {
    try {
      const res = await getCandleApi(stockCode, type, startTime, endTime);
      console.log('캔들 응답:', res);
      setCandles(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    if (!stockCode) return;
    fetchCandle();
  }, [stockCode, type, startTime, endTime]);

  return { candles };
}