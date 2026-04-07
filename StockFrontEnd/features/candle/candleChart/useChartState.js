// CandleChart/useChartState.js
import { useRef } from "react";
import { INTERVAL_MS, DEFAULT_VIEW, RIGHT_PAD } from "./CandleConstants";

export function useChartState() {
  const stateRef = useRef({
    candles:      [],
    intervalType: "ONE_MINUTE",
    startMs:      0,
    visibleCount: DEFAULT_VIEW,
    minV:         0,
    maxV:         1,
    yInited:      false,
  });

  const getIv    = () => INTERVAL_MS[stateRef.current.intervalType] ?? 60_000;
  const getEndMs = () => stateRef.current.startMs + stateRef.current.visibleCount * getIv();

  const initView = (candles, intervalType) => {
    const iv      = INTERVAL_MS[intervalType] ?? 60_000;
    const lastMs  = new Date(candles[candles.length - 1].time).getTime();
    const endMs   = lastMs + RIGHT_PAD * iv;
    stateRef.current.startMs      = endMs - DEFAULT_VIEW * iv;
    stateRef.current.visibleCount = DEFAULT_VIEW;
    stateRef.current.yInited      = false;
  };

  return { stateRef, getIv, getEndMs, initView };
}