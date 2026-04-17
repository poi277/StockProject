'use client';

import { useEffect, useRef, useState } from 'react';
import { createChart, ColorType, CandlestickSeries, CrosshairMode } from 'lightweight-charts';

// 📊 테스트 데이터
const data = [
  { time: 1712700000, open: 100, high: 105, low: 95, close: 102 },
  { time: 1712700600, open: 102, high: 108, low: 101, close: 107 },
  { time: 1712701200, open: 107, high: 110, low: 106, close: 109 },
  { time: 1712701800, open: 109, high: 112, low: 108, close: 111 },
];

// CSS 변수 읽기
function getCssVar(name) {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
}

// 차트 생성
function initChart(container) {
  const bgColor = getCssVar('--wts-adaptive-background');
  return createChart(container, {
    layout: { background: { type: ColorType.Solid, color: bgColor }, textColor: '#8b949e' },
    grid: { vertLines: { color: 'rgba(255,255,255,0.04)' }, horzLines: { color: 'rgba(255,255,255,0.06)' } },
    crosshair: {
      mode: CrosshairMode.Normal,
      vertLine: { width: 1, color: 'rgba(200,200,200,0.3)', style: 3, labelBackgroundColor: getCssVar('--wts-adaptive-blue500') },
      horzLine: { width: 1, color: 'rgba(200,200,200,0.3)', style: 3, labelBackgroundColor: getCssVar('--wts-adaptive-blue500') },
    },
    rightPriceScale: { borderColor: 'rgba(255,255,255,0.05)', scaleMargins: { top: 0.15, bottom: 0.15 } },
    timeScale: { borderColor: 'rgba(255,255,255,0.05)', timeVisible: true, secondsVisible: false },
    width: container.clientWidth,
    height: container.clientHeight
  });
}

// 캔들 생성
function createCandleSeries(chart) {
  return chart.addSeries(CandlestickSeries, {
    upColor: '#fc2d4c',
    downColor: '#007ff3',
    borderVisible: false,
    wickUpColor: '#fc2d4c',
    wickDownColor: '#007ff3',
  });
}

export default function ChartComponent() {
  const containerRef = useRef(null);
  const [ohlc, setOhlc] = useState(null);

  useEffect(() => {
    if (!containerRef.current) return;

    const chart = initChart(containerRef.current);
    const candleSeries = createCandleSeries(chart);
    candleSeries.setData(data);
    chart.timeScale().fitContent();

    const resizeObserver = new ResizeObserver(entries => {
      const { width, height } = entries[0].contentRect;
      chart.resize(width, height);
    });
    resizeObserver.observe(containerRef.current);

    chart.subscribeCrosshairMove((param) => {
      if (!param || !param.time || !param.seriesData) { setOhlc(null); return; }
      const bar = param.seriesData.get(candleSeries);
      if (bar) setOhlc(bar);
    });

    return () => {
      resizeObserver.disconnect();
      chart.remove();
    };
  }, []);

  return (
    <div className='mnc8st5'>
      <div className='mnc8st6' data-multi-chart-loading='false'>
          <div ref={containerRef} style={{ width: '100%', height:'100%' }}>
            {ohlc && ( <div style={{  position: 'absolute', top: 8, left: 8, zIndex: 10, fontSize: 12, color: '#8b949e',  pointerEvents: 'none', }}>
                시작: {ohlc.open} | 고가: {ohlc.high} | 저가: {ohlc.low} | 종가: {ohlc.close}
              </div>
            )}
          </div>
        </div>
      </div>
  );
}