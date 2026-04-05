// CandleChart/useCrosshair.js
import { formatTime } from "./utils";

export function useCrosshair({ crossRef, mainRef, stateRef, getIv, setOhlc }) {

  const drawCross = (cx, cy) => {
    const cross  = crossRef.current;
    const canvas = mainRef.current;
    if (!cross || !canvas) return;

    const W = cross.width, H = cross.height;
    const cctx = cross.getContext("2d");
    cctx.clearRect(0, 0, W, H);

    const padL = 10, padR = 65, padT = 20, padB = 36;

    if (cx < padL || cx > W - padR || cy < padT || cy > H - padB) {
      setOhlc(null);
      return;
    }

    const s       = stateRef.current;
    const iv      = getIv();
    const startMs = s.startMs;
    const endMs   = s.startMs + s.visibleCount * iv;
    const totalMs = endMs - startMs;
    const cw      = W - padL - padR;
    const ch      = H - padT - padB;
    const { minV, maxV } = s;

    // 크로스헤어 선
    cctx.strokeStyle = "rgba(200,200,200,0.4)"; cctx.lineWidth = 0.5; cctx.setLineDash([4, 4]);
    cctx.beginPath(); cctx.moveTo(cx, padT);  cctx.lineTo(cx, H - padB); cctx.stroke();
    cctx.beginPath(); cctx.moveTo(padL, cy);  cctx.lineTo(W - padR, cy); cctx.stroke();
    cctx.setLineDash([]);

    // Y축 가격 레이블
    const price      = maxV - ((cy - padT) / ch) * (maxV - minV);
    const priceLabel = Math.round(price).toLocaleString();
    const priceBoxW  = padR - 4;
    const priceBoxH  = 18;
    const priceBoxX  = W - padR + 2;
    const priceBoxY  = cy - priceBoxH / 2;

    cctx.fillStyle = "#2962ff";
    cctx.beginPath();
    cctx.roundRect(priceBoxX, priceBoxY, priceBoxW, priceBoxH, 3);
    cctx.fill();
    cctx.fillStyle = "#ffffff"; cctx.font = "bold 10px sans-serif"; cctx.textAlign = "center";
    cctx.fillText(priceLabel, priceBoxX + priceBoxW / 2, cy + 4);

    // X축 시간 레이블
    const mouseT    = startMs + ((cx - padL) / cw) * totalMs;
    const d         = new Date(mouseT);
    const timeLabel = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,"0")}-${String(d.getDate()).padStart(2,"0")} ${String(d.getHours()).padStart(2,"0")}:${String(d.getMinutes()).padStart(2,"0")}`;
    const timeBoxW  = 130;
    const timeBoxH  = 18;
    const timeBoxX  = Math.max(padL, Math.min(W - padR - timeBoxW, cx - timeBoxW / 2));
    const timeBoxY  = H - padB + 2;

    cctx.fillStyle = "#2962ff";
    cctx.beginPath();
    cctx.roundRect(timeBoxX, timeBoxY, timeBoxW, timeBoxH, 3);
    cctx.fill();
    cctx.fillStyle = "#ffffff"; cctx.font = "bold 10px sans-serif"; cctx.textAlign = "center";
    cctx.fillText(timeLabel, timeBoxX + timeBoxW / 2, timeBoxY + 13);

    // 가장 가까운 캔들 → OHLC 업데이트
    if (!s.candles.length) return;
    const c = s.candles.reduce((best, cur) => {
      const bd = Math.abs(new Date(best.time).getTime() - mouseT);
      const cd = Math.abs(new Date(cur.time).getTime() - mouseT);
      return cd < bd ? cur : best;
    });
    if (c) setOhlc(c);
  };

  const clearCross = () => {
    const cctx = crossRef.current?.getContext("2d");
    if (cctx) cctx.clearRect(0, 0, crossRef.current.width, crossRef.current.height);
  };

  return { drawCross, clearCross };
}