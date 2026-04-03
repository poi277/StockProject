// CandleChart.jsx
import { useEffect, useRef, useState } from "react";

function formatTime(ts) {
  const d  = new Date(ts);
  const hh = String(d.getHours()).padStart(2, "0");
  const mn = String(d.getMinutes()).padStart(2, "0");
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${mm}/${dd} ${hh}:${mn}`;
}

const INTERVAL_MS = {
  ONE_MINUTE:     60_000,
  THREE_MINUTE:   180_000,
  FIVE_MINUTE:    300_000,
  TEN_MINUTE:     600_000,
  FIFTEEN_MINUTE: 900_000,
  THIRTY_MINUTE:  1_800_000,
  SIXTY_MINUTE:   3_600_000,
};

const DEFAULT_VIEW = 100;
const RIGHT_PAD    = 20;

export default function CandleChart({ candles = [], onEdgeReached, intervalType = "ONE_MINUTE" }) {
  const wrapRef    = useRef(null);
  const mainRef    = useRef(null);
  const crossRef   = useRef(null);
  const tooltipRef = useRef(null);
  const drawRef    = useRef(null);
  const onEdgeRef  = useRef(onEdgeReached);
  const edgeDebounceRef = useRef(null); // ✅ 문제1: debounce용

  const viewRef         = useRef({ startMs: 0, visibleCount: DEFAULT_VIEW });
  const candlesRef      = useRef(candles);
  const intervalTypeRef = useRef(intervalType);
  const drag            = useRef({ active: false, startX: 0, startMs: 0 });
  const yRangeRef       = useRef({ minV: 0, maxV: 1 }); // ✅ 문제3: Y범위 고정용

  const [ohlc,       setOhlc]       = useState(null);
  const [range,      setRange]      = useState("");
  const [isDragging, setIsDragging] = useState(false);

  useEffect(() => { candlesRef.current      = candles;       }, [candles]);
  useEffect(() => { intervalTypeRef.current = intervalType;  }, [intervalType]);
  useEffect(() => { onEdgeRef.current       = onEdgeReached; }, [onEdgeReached]);

  const getIntervalMs = () => INTERVAL_MS[intervalTypeRef.current] ?? 60_000;

  const getViewRange = () => {
    const iv = getIntervalMs();
    const { startMs, visibleCount } = viewRef.current;
    return { startMs, endMs: startMs + visibleCount * iv, iv };
  };

  const draw = (lockY = false) => {
    const canvas = mainRef.current;
    const wrap   = wrapRef.current;
    if (!canvas || !wrap) return;

    const W = canvas.width, H = canvas.height;
    const padL = 10, padR = 65, padT = 20, padB = 36;
    const cw = W - padL - padR, ch = H - padT - padB;

    const { startMs, endMs, iv } = getViewRange();
    const totalMs = endMs - startMs;

    const ctx = canvas.getContext("2d");
    ctx.clearRect(0, 0, W, H);
    ctx.fillStyle = "#131722";
    ctx.fillRect(0, 0, W, H);

    // 뷰 안에 있는 캔들만
    const visible = candlesRef.current.filter(c => {
      const t = new Date(c.time).getTime();
      return t >= startMs && t <= endMs;
    });

    // ✅ 문제2: visible 없어도 그리드/축은 항상 그리기
    // Y범위 계산 (lockY=true면 이전 범위 유지)
    if (!lockY && visible.length) {
      const allVals = visible.flatMap(c => [c.high, c.low]);
      let minV = Math.min(...allVals), maxV = Math.max(...allVals);
      const p = (maxV - minV) * 0.1;
      minV -= p; maxV += p;
      if (minV === maxV) { minV -= 1; maxV += 1; }
      yRangeRef.current = { minV, maxV };
    }

    const { minV, maxV } = yRangeRef.current;
    const xOf = (ts) => padL + ((ts - startMs) / totalMs) * cw;
    const yOf = (v)  => padT + ch - ((v - minV) / (maxV - minV)) * ch;

    // 그리드 & Y축
    ctx.strokeStyle = "#2a2e39"; ctx.lineWidth = 0.5;
    for (let i = 0; i <= 5; i++) {
      const y   = padT + (ch / 5) * i;
      const val = maxV - ((maxV - minV) / 5) * i;
      ctx.beginPath(); ctx.moveTo(padL, y); ctx.lineTo(W - padR, y); ctx.stroke();
      ctx.fillStyle = "#787b86"; ctx.font = "10px sans-serif"; ctx.textAlign = "left";
      ctx.fillText(Math.round(val).toLocaleString(), W - padR + 4, y + 3);
    }

    // 캔들
    if (visible.length) {
      const bw = Math.max(1, (iv / totalMs) * cw * 0.7);
      visible.forEach(c => {
        const t     = new Date(c.time).getTime();
        const x     = xOf(t);
        const isUp  = c.close >= c.open;
        const color = isUp ? "#26a69a" : "#ef5350";
        ctx.strokeStyle = color; ctx.fillStyle = color; ctx.lineWidth = 1;
        ctx.beginPath(); ctx.moveTo(x, yOf(c.high)); ctx.lineTo(x, yOf(c.low)); ctx.stroke();
        const top = yOf(Math.max(c.open, c.close));
        const bot = yOf(Math.min(c.open, c.close));
        ctx.fillRect(x - bw / 2, top, bw, Math.max(1, bot - top));
      });
    }

    // X축 레이블
    ctx.fillStyle = "#787b86"; ctx.font = "10px sans-serif"; ctx.textAlign = "center";
    const labelCount = 6;
    let prevDate = null;
    for (let i = 0; i <= labelCount; i++) {
      const t = startMs + (totalMs / labelCount) * i;
      const x = xOf(t);
      if (x < padL || x > W - padR) continue;
      const d = new Date(t);
      const label = (prevDate !== null && prevDate === d.getDate())
        ? `${String(d.getHours()).padStart(2,"0")}:${String(d.getMinutes()).padStart(2,"0")}`
        : `${d.getMonth()+1}/${d.getDate()}`;
      prevDate = d.getDate();
      ctx.fillText(label, x, H - padB + 14);
    }

    setRange(`${formatTime(startMs)} ~ ${formatTime(endMs)}`);

    // ✅ 문제1: debounce로 onEdgeReached 호출 제한
    if (candlesRef.current.length) {
      const firstCandleMs = new Date(candlesRef.current[0].time).getTime();
      if (startMs <= firstCandleMs) {
        clearTimeout(edgeDebounceRef.current);
        edgeDebounceRef.current = setTimeout(() => {
          onEdgeRef.current?.("left");
        }, 500);
      }
    }
  };

  drawRef.current = draw;

  const drawCross = (cx, cy, dispX, dispY) => {
    const cross  = crossRef.current;
    const canvas = mainRef.current;
    if (!cross || !canvas) return;
    const W = cross.width, H = cross.height;
    const cctx = cross.getContext("2d");
    cctx.clearRect(0, 0, W, H);

    const padL = 10, padR = 65, padT = 20, padB = 36;

    if (cx < padL || cx > W - padR || cy < padT || cy > H - padB) {
      setOhlc(null);
      if (tooltipRef.current) tooltipRef.current.style.display = "none";
      return;
    }

    cctx.strokeStyle = "rgba(200,200,200,0.3)"; cctx.lineWidth = 0.5; cctx.setLineDash([4, 4]);
    cctx.beginPath(); cctx.moveTo(cx, padT);  cctx.lineTo(cx, H - padB); cctx.stroke();
    cctx.beginPath(); cctx.moveTo(padL, cy);  cctx.lineTo(W - padR, cy); cctx.stroke();
    cctx.setLineDash([]);

    if (!candlesRef.current.length) return;

    const { startMs, endMs } = getViewRange();
    const totalMs = endMs - startMs;
    const cw      = W - padL - padR;
    const mouseT  = startMs + ((cx - padL) / cw) * totalMs;

    const c = candlesRef.current.reduce((best, cur) => {
      const bd = Math.abs(new Date(best.time).getTime() - mouseT);
      const cd = Math.abs(new Date(cur.time).getTime() - mouseT);
      return cd < bd ? cur : best;
    });
    if (!c) return;

    setOhlc(c);
    const tip = tooltipRef.current;
    if (!tip) return;
    tip.style.color   = c.close >= c.open ? "#26a69a" : "#ef5350";
    tip.textContent   = `${formatTime(c.time)}  O:${Math.round(c.open).toLocaleString()} H:${Math.round(c.high).toLocaleString()} L:${Math.round(c.low).toLocaleString()} C:${Math.round(c.close).toLocaleString()}`;
    tip.style.display = "block";
    const tw = tip.offsetWidth;
    tip.style.left = (dispX + 10 + tw > W - 70 ? dispX - tw - 10 : dispX + 10) + "px";
    tip.style.top  = (dispY > 60 ? dispY - 40 : dispY + 10) + "px";
  };

  const resize = () => {
    const wrap = wrapRef.current, main = mainRef.current, cross = crossRef.current;
    if (!wrap || !main || !cross) return;
    const w = wrap.clientWidth, h = Math.round(w * 0.5);
    main.width  = w; main.height  = h;
    cross.width = w; cross.height = h;
    cross.style.width = w + "px"; cross.style.height = h + "px";
    drawRef.current();
  };

  useEffect(() => {
    if (!candles.length) return;
    const iv      = INTERVAL_MS[intervalType] ?? 60_000;
    const lastMs  = new Date(candles[candles.length - 1].time).getTime();
    const endMs   = lastMs + RIGHT_PAD * iv;
    const startMs = endMs - DEFAULT_VIEW * iv;
    viewRef.current = { startMs, visibleCount: DEFAULT_VIEW };
    resize();
  }, [candles, intervalType]);

  useEffect(() => {
    const ob = new ResizeObserver(resize);
    if (wrapRef.current) ob.observe(wrapRef.current);
    return () => ob.disconnect();
  }, []);

  useEffect(() => {
    const canvas = mainRef.current;
    if (!canvas) return;
    const handleWheel = (e) => {
      e.preventDefault();
      const v         = viewRef.current;
      const iv        = getIntervalMs();
      const prevCount = v.visibleCount;
      const zoomStep  = Math.max(5, Math.round(prevCount * 0.1));
      const nextCount = Math.max(20, prevCount + (e.deltaY > 0 ? zoomStep : -zoomStep));
      const rect      = canvas.getBoundingClientRect();
      const ratio     = (e.clientX - rect.left) / rect.width;
      const { startMs, endMs } = getViewRange();
      const anchorMs   = startMs + (endMs - startMs) * ratio;
      v.startMs      = anchorMs - nextCount * iv * ratio;
      v.visibleCount = nextCount;
      drawRef.current(true); // Y축 재계산
    };
    canvas.addEventListener("wheel", handleWheel, { passive: false });
    return () => canvas.removeEventListener("wheel", handleWheel);
  }, []);

  useEffect(() => {
    const onMove = (e) => {
      const canvas = mainRef.current;
      if (!canvas) return;

      if (drag.current.active) {
        const iv      = getIntervalMs();
        const rect    = canvas.getBoundingClientRect();
        const pxPerMs = rect.width / (viewRef.current.visibleCount * iv);
        const dxMs    = -(e.clientX - drag.current.startX) / pxPerMs;
        viewRef.current.startMs = drag.current.startMs + dxMs;
        drawRef.current(false); // ✅ 문제3: 드래그 중 Y축 고정
        const cctx = crossRef.current?.getContext("2d");
        if (cctx) cctx.clearRect(0, 0, crossRef.current.width, crossRef.current.height);
        if (tooltipRef.current) tooltipRef.current.style.display = "none";
        return;
      }

      const rect   = canvas.getBoundingClientRect();
      const scaleX = canvas.width  / rect.width;
      const scaleY = canvas.height / rect.height;
      drawCross(
        (e.clientX - rect.left) * scaleX,
        (e.clientY - rect.top)  * scaleY,
        e.clientX - rect.left,
        e.clientY - rect.top
      );
    };

    const onUp = () => {
      if (drag.current.active) {
        drag.current.active = false;
        setIsDragging(false);
        drawRef.current(false); // ✅ 드래그 끝나면 Y축 재계산
      }
    };

    window.addEventListener("mousemove", onMove);
    window.addEventListener("mouseup",   onUp);
    return () => {
      window.removeEventListener("mousemove", onMove);
      window.removeEventListener("mouseup",   onUp);
    };
  }, []);

  const onMouseDown = (e) => {
    drag.current = { active: true, startX: e.clientX, startMs: viewRef.current.startMs };
    setIsDragging(true);
  };

  return (
    <>
      <div style={{ display: "flex", alignItems: "center", padding: "6px 14px", background: "#1e222d", fontSize: 11, minHeight: 28 }}>
        {ohlc && (
          <span style={{ color: ohlc.close >= ohlc.open ? "#26a69a" : "#ef5350" }}>
            {formatTime(ohlc.time)}&nbsp;&nbsp;
            시작 {Math.round(ohlc.open).toLocaleString()}&nbsp;
            고가 {Math.round(ohlc.high).toLocaleString()}&nbsp;
            저가 {Math.round(ohlc.low).toLocaleString()}&nbsp;
            종가 {Math.round(ohlc.close).toLocaleString()}
          </span>
        )}
      </div>

      <div
        ref={wrapRef}
        style={{ position: "relative", userSelect: "none", cursor: isDragging ? "grabbing" : "crosshair" }}
      >
        <canvas ref={mainRef} style={{ display: "block", width: "100%" }} onMouseDown={onMouseDown} />
        <canvas ref={crossRef} style={{ position: "absolute", top: 0, left: 0, pointerEvents: "none" }} />
        <div
          ref={tooltipRef}
          style={{
            position: "absolute", display: "none",
            background: "#1e222d", border: "0.5px solid #363a45",
            borderRadius: 4, padding: "6px 10px",
            fontSize: 11, pointerEvents: "none", whiteSpace: "nowrap", zIndex: 10,
          }}
        />
      </div>

      <div style={{ display: "flex", justifyContent: "space-between", padding: "8px 14px", background: "#1e222d", borderTop: "0.5px solid #2a2e39", fontSize: 11, color: "#787b86" }}>
        <span>← 드래그로 이동 | 휠로 확대/축소</span>
        <span>{range}</span>
      </div>
    </>
  );
}