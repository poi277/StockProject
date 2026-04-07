// CandleChart/useDraw.js
import { LABEL_MULTIPLES } from "./CandleConstants";
import { formatTime } from "./utils";

const UP_COLOR   = "#ef5350"; // 빨강 (상승)
const DOWN_COLOR = "#0056e0"; // 파랑 (하락)

export function useDraw({ mainRef, stateRef, getIv, setRange, onEdgeRef, edgeDebounce }) {

  const draw = (recalcY = false) => {
    const canvas = mainRef.current;
    if (!canvas) return;

    const s  = stateRef.current;
    const W  = canvas.width, H = canvas.height;
    const padL = 10, padR = 65, padT = 20, padB = 36;
    const cw = W - padL - padR, ch = H - padT - padB;

    const iv      = getIv();
    const startMs = s.startMs;
    const endMs   = s.startMs + s.visibleCount * iv;
    const totalMs = endMs - startMs;

    const ctx = canvas.getContext("2d");
    ctx.clearRect(0, 0, W, H);
    ctx.fillStyle = "#131722";
    ctx.fillRect(0, 0, W, H);

    const visible = s.candles.filter(c => {
      const t = new Date(c.time).getTime();
      return t >= startMs && t <= endMs;
    });

    if ((recalcY || !s.yInited) && visible.length) {
      const vals = visible.flatMap(c => [c.high, c.low]);
      let minV = Math.min(...vals), maxV = Math.max(...vals);
      const p  = (maxV - minV) * 0.1;
      minV -= p; maxV += p;
      if (minV === maxV) { minV -= 1; maxV += 1; }
      s.minV    = minV;
      s.maxV    = maxV;
      s.yInited = true;
    }

    const { minV, maxV } = s;
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

    // 마지막 캔들 현재가 라인 + 레이블
    if (s.candles.length) {
      const lastCandle = s.candles[s.candles.length - 1];
      const lastPrice  = lastCandle.close;

      if (lastPrice >= minV && lastPrice <= maxV) {
        const lastPriceY = yOf(lastPrice);
        const priceLabel = Math.round(lastPrice).toLocaleString();
        const isUp       = lastCandle.close >= lastCandle.open;
        const color      = isUp ? UP_COLOR : DOWN_COLOR;

        ctx.strokeStyle = color;
        ctx.lineWidth   = 0.8;
        ctx.setLineDash([4, 4]);
        ctx.beginPath();
        ctx.moveTo(padL, lastPriceY);
        ctx.lineTo(W - padR, lastPriceY);
        ctx.stroke();
        ctx.setLineDash([]);

        const boxW = padR - 4;
        const boxH = 18;
        const boxX = W - padR + 2;
        const boxY = lastPriceY - boxH / 2;

        ctx.fillStyle = color;
        ctx.beginPath();
        ctx.roundRect(boxX, boxY, boxW, boxH, 3);
        ctx.fill();

        ctx.fillStyle = "#ffffff";
        ctx.font      = "bold 10px sans-serif";
        ctx.textAlign = "center";
        ctx.fillText(priceLabel, boxX + boxW / 2, lastPriceY + 4);
      }
    }

    // 캔들
    if (visible.length) {
      const bw = Math.max(1, (iv / totalMs) * cw * 0.7);
      visible.forEach(c => {
        const x     = xOf(new Date(c.time).getTime());
        const isUp  = c.close >= c.open;
        const color = isUp ? UP_COLOR : DOWN_COLOR;
        ctx.strokeStyle = color; ctx.fillStyle = color; ctx.lineWidth = 1;
        ctx.beginPath(); ctx.moveTo(x, yOf(c.high)); ctx.lineTo(x, yOf(c.low)); ctx.stroke();
        const top = yOf(Math.max(c.open, c.close));
        const bot = yOf(Math.min(c.open, c.close));
        ctx.fillRect(x - bw / 2, top, bw, Math.max(1, bot - top));
      });
    }

    // X축 눈금
    const labelIntervalMs   = LABEL_MULTIPLES.find(m => m >= totalMs / 6) ?? LABEL_MULTIPLES[LABEL_MULTIPLES.length - 1];
    const firstVisibleLabel = Math.ceil(startMs / labelIntervalMs) * labelIntervalMs;
    const firstLabel        = firstVisibleLabel - labelIntervalMs;

    ctx.font = "10px sans-serif"; ctx.textAlign = "center";
    let prevDate = null;

    for (let t = firstLabel; t <= endMs + labelIntervalMs; t += labelIntervalMs) {
      const x     = xOf(t);
      const d     = new Date(t);
      const isNew = prevDate === null || prevDate !== d.getDate();
      const label = isNew
        ? `${d.getMonth()+1}/${d.getDate()}`
        : `${String(d.getHours()).padStart(2,"0")}:${String(d.getMinutes()).padStart(2,"0")}`;
      prevDate = d.getDate();

      if (x >= padL && x <= W - padR) {
        ctx.strokeStyle = "#363a45"; ctx.lineWidth = 0.5;
        ctx.beginPath(); ctx.moveTo(x, H - padB); ctx.lineTo(x, H - padB + 4); ctx.stroke();
      }
      if (x >= padL + 20 && x <= W - padR - 20) {
        ctx.fillStyle = "#787b86";
        ctx.fillText(label, x, H - padB + 14);
      }
    }

    setRange(`${formatTime(startMs)} ~ ${formatTime(endMs)}`);

    if (s.candles.length) {
      const firstMs = new Date(s.candles[0].time).getTime();
      if (startMs <= firstMs) {
        clearTimeout(edgeDebounce.current);
        edgeDebounce.current = setTimeout(() => onEdgeRef.current?.("left"), 500);
      }
    }
  };

  return { draw };
}