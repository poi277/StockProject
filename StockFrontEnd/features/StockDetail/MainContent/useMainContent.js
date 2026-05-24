import { useState, useRef, useCallback, useEffect } from 'react';

const INIT = {
  splitV1: 759,
  splitV2: 1089,
  splitH_left: 395,
  splitH_mid: 562,
  splitH_right: 340,
  totalH: 727,
  GAP: 10,
};

export default function useMainContent() {
  const containerRef = useRef(null);
  const [selectedPrice, setSelectedPrice] = useState({ value: null });

  const [layout, setLayout] = useState({
    splitV1: INIT.splitV1,
    splitV2: INIT.splitV2,
    splitH_left: INIT.splitH_left,
    splitH_mid: INIT.splitH_mid,
    splitH_right: INIT.splitH_right,
    totalH: INIT.totalH,
  });

  const dragging = useRef(null);

  const handlePriceSelect = (price) => {
    setSelectedPrice({ value: price });
};

  const onMouseDown = useCallback((e, type) => {
    e.preventDefault();
    dragging.current = {
      type,
      startX: e.clientX,
      startY: e.clientY,
      startLayout: { ...layout },
    };
  }, [layout]);

  useEffect(() => {
    const onMouseMove = (e) => {
      if (!dragging.current) return;
      const { type, startX, startY, startLayout } = dragging.current;
      const dx = e.clientX - startX;
      const dy = e.clientY - startY;
      const G = INIT.GAP;

      setLayout(prev => {
        const next = { ...prev };
        if (type === 'v1') {
          next.splitV1 = Math.max(200, Math.min(startLayout.splitV1 + dx, startLayout.splitV2 - 200));
        } else if (type === 'v2') {
          next.splitV2 = Math.max(startLayout.splitV1 + 200, Math.min(startLayout.splitV2 + dx, 1600));
        } else if (type === 'h_left') {
          next.splitH_left = Math.max(100, Math.min(startLayout.splitH_left + dy, startLayout.totalH - 100));
        } else if (type === 'h_mid') {
          next.splitH_mid = Math.max(100, Math.min(startLayout.splitH_mid + dy, startLayout.totalH - 100));
        } else if (type === 'h_right') {
          next.splitH_right = Math.max(100, Math.min(startLayout.splitH_right + dy, startLayout.totalH - 100));
        }
        return next;
      });
    };

    const onMouseUp = () => { dragging.current = null; };

    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);
    return () => {
      window.removeEventListener('mousemove', onMouseMove);
      window.removeEventListener('mouseup', onMouseUp);
    };
  }, []);

  const { splitV1, splitV2, splitH_left, splitH_mid, splitH_right, totalH } = layout;
  const G = INIT.GAP;
  const w1 = splitV1;
  const w2 = splitV2 - splitV1 - G;
  const w3 = 1460 - splitV2 - G;

  return {
    containerRef,
    selectedPrice, setSelectedPrice,handlePriceSelect,
    onMouseDown,
    splitV1, splitV2, splitH_left, splitH_mid, splitH_right, totalH,
    G, w1, w2, w3,
  };
}