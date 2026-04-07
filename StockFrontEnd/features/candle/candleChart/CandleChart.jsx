// CandleChart/CandleChart.jsx
import { useEffect, useRef, useState } from "react";
import { useChartState } from "./useChartState";
import { useDraw } from "./useDraw";
import { useCrosshair } from "./useCrosshair";
import { useEvents } from "./useEvents";
import { formatTime } from "./utils";

const UP_COLOR   = "#ef5350"; // 빨강 (상승)
const DOWN_COLOR = "#0056e0"; // 파랑 (하락)

export default function CandleChart({ candles = [], onEdgeReached, intervalType = "ONE_MINUTE" }) {
  const wrapRef  = useRef(null);
  const mainRef  = useRef(null);
  const crossRef = useRef(null);

  const onEdgeRef    = useRef(onEdgeReached);
  const edgeDebounce = useRef(null);
  const drag         = useRef({ active: false, startX: 0, startMs: 0 });

  const [ohlc,       setOhlc]       = useState(null);
  const [range,      setRange]      = useState("");
  const [isDragging, setIsDragging] = useState(false);

  const { stateRef, getIv, getEndMs, initView } = useChartState();
  const { draw }                                 = useDraw({ mainRef, stateRef, getIv, setRange, onEdgeRef, edgeDebounce });
  const { drawCross, clearCross }                = useCrosshair({ crossRef, mainRef, stateRef, getIv, setOhlc });

  useEffect(() => { stateRef.current.candles      = candles;       }, [candles]);
  useEffect(() => { stateRef.current.intervalType = intervalType;  }, [intervalType]);
  useEffect(() => { onEdgeRef.current             = onEdgeReached; }, [onEdgeReached]);

  const resize = () => {
    const wrap = wrapRef.current, main = mainRef.current, cross = crossRef.current;
    if (!wrap || !main || !cross) return;
    const w = wrap.clientWidth, h = Math.round(w * 0.5);
    main.width  = w; main.height  = h;
    cross.width = w; cross.height = h;
    cross.style.width = w + "px"; cross.style.height = h + "px";
    draw();
  };

  useEvents({
    mainRef, crossRef, wrapRef,
    stateRef, getIv, getEndMs,
    draw, drawCross, clearCross,
    drag, setIsDragging,
    candles, intervalType,
    initView, resize,
  });

  const onMouseDown = (e) => {
    drag.current = { active: true, startX: e.clientX, startMs: stateRef.current.startMs };
    setIsDragging(true);
  };

  return (
    <>
      {/* OHLC 상단 바 */}
      <div style={{ display: "flex", alignItems: "center", padding: "6px 14px", background: "#1e222d", fontSize: 11, minHeight: 28 }}>
        {ohlc && (
          <span style={{ color: ohlc.close >= ohlc.open ? UP_COLOR : DOWN_COLOR }}>
            {formatTime(ohlc.time)}&nbsp;&nbsp;
            시작 {Math.round(ohlc.open).toLocaleString()}&nbsp;
            고가 {Math.round(ohlc.high).toLocaleString()}&nbsp;
            저가 {Math.round(ohlc.low).toLocaleString()}&nbsp;
            종가 {Math.round(ohlc.close).toLocaleString()}
          </span>
        )}
      </div>

      {/* 캔버스 */}
      <div
        ref={wrapRef}
        style={{ position: "relative", userSelect: "none", cursor: isDragging ? "grabbing" : "crosshair" }}
      >
        <canvas ref={mainRef} style={{ display: "block", width: "100%" }} onMouseDown={onMouseDown} />
        <canvas ref={crossRef} style={{ position: "absolute", top: 0, left: 0, pointerEvents: "none" }} />
      </div>

      {/* 하단 범위 */}
      <div style={{ display: "flex", justifyContent: "space-between", padding: "8px 14px", background: "#1e222d", borderTop: "0.5px solid #2a2e39", fontSize: 11, color: "#787b86" }}>
        <span>← 드래그로 이동 | 휠로 확대/축소</span>
        <span>{range}</span>
      </div>
    </>
  );
}