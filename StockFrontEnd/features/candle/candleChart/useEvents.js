// CandleChart/useEvents.js
import { useEffect } from "react";
import { INTERVAL_MS } from "./constants";

export function useEvents({
  mainRef, crossRef, wrapRef,
  stateRef, getIv, getEndMs,
  draw, drawCross, clearCross,
  drag, setIsDragging,
  candles, intervalType,
  initView, resize,
}) {
  // candles / intervalType 변경 시 뷰 초기화
  useEffect(() => {
    if (!candles.length) return;
    initView(candles, intervalType);
    resize();
  }, [candles, intervalType]);

  // ResizeObserver
  useEffect(() => {
    const ob = new ResizeObserver(resize);
    if (wrapRef.current) ob.observe(wrapRef.current);
    return () => ob.disconnect();
  }, []);

  // 휠 줌
  useEffect(() => {
    const canvas = mainRef.current;
    if (!canvas) return;
    const onWheel = (e) => {
      e.preventDefault();
      const s         = stateRef.current;
      const iv        = getIv();
      const prevCount = s.visibleCount;
      const zoomStep  = Math.max(5, Math.round(prevCount * 0.1));
      const nextCount = Math.max(20, prevCount + (e.deltaY > 0 ? zoomStep : -zoomStep));
      const rect      = canvas.getBoundingClientRect();
      const ratio     = (e.clientX - rect.left) / rect.width;
      const startMs   = s.startMs;
      const endMs     = getEndMs();
      const anchorMs  = startMs + (endMs - startMs) * ratio;
      s.startMs      = anchorMs - nextCount * iv * ratio;
      s.visibleCount = nextCount;
      draw(true);
    };
    canvas.addEventListener("wheel", onWheel, { passive: false });
    return () => canvas.removeEventListener("wheel", onWheel);
  }, []);

  // 드래그 & 크로스헤어
  useEffect(() => {
    const onMove = (e) => {
      const canvas = mainRef.current;
      if (!canvas) return;

      if (drag.current.active) {
        const s       = stateRef.current;
        const iv      = getIv();
        const rect    = canvas.getBoundingClientRect();
        const pxPerMs = rect.width / (s.visibleCount * iv);
        s.startMs     = drag.current.startMs - (e.clientX - drag.current.startX) / pxPerMs;
        draw(false);
        clearCross();
        return;
      }

      const rect   = canvas.getBoundingClientRect();
      const scaleX = canvas.width  / rect.width;
      const scaleY = canvas.height / rect.height;
      drawCross(
        (e.clientX - rect.left) * scaleX,
        (e.clientY - rect.top)  * scaleY,
      );
    };

    const onUp = () => {
      if (!drag.current.active) return;
      drag.current.active = false;
      setIsDragging(false);
      draw(false);
    };

    window.addEventListener("mousemove", onMove);
    window.addEventListener("mouseup",   onUp);
    return () => {
      window.removeEventListener("mousemove", onMove);
      window.removeEventListener("mouseup",   onUp);
    };
  }, []);
}