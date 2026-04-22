'use client';

import { useEffect, useRef, useState } from 'react';
import { createChart, ColorType, CandlestickSeries, CrosshairMode } from 'lightweight-charts';
import useCandle from './useChart';

function getCssVar(name) {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
}

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
    localization: {
      priceFormatter: (price) => Math.round(price).toLocaleString(),
      timeFormatter: (time) => {
        const date = new Date(time * 1000);
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        const h = String(date.getHours()).padStart(2, '0');
        const min = String(date.getMinutes()).padStart(2, '0');
        return `${y}-${m}-${d} ${h}:${min}`;
      },
    },
    timeScale: { borderColor: 'rgba(255,255,255,0.05)', timeVisible: true, secondsVisible: false },
    width: container.clientWidth,
    height: container.clientHeight,
  });
}

function createCandleSeries(chart) {
  return chart.addSeries(CandlestickSeries, {
    upColor: '#fc2d4c',
    downColor: '#007ff3',
    borderVisible: false,
    wickUpColor: '#fc2d4c',
    wickDownColor: '#007ff3',
  });
}

function toUnixTime(timeStr) {
  return Math.floor(new Date(timeStr).getTime() / 1000);
}

export default function ChartComponent({ stockCode, type = 'ONE_MINUTE' }) {
  const containerRef = useRef(null);
  const chartRef = useRef(null);
  const candleSeriesRef = useRef(null);
  const [ohlc, setOhlc] = useState(null);
  const isLoadingMore = useRef(false);
  const { candles, loadMoreCandles } = useCandle(stockCode, type);

  // 차트 초기화 (한 번만)
  useEffect(() => {
    if (!containerRef.current) return;
    const chart = initChart(containerRef.current);
    const candleSeries = createCandleSeries(chart);
    chartRef.current = chart;
    candleSeriesRef.current = candleSeries;

    chart.timeScale().subscribeVisibleLogicalRangeChange((range) => {
      if (!range) return;
      if (range.from < 5 && !isLoadingMore.current) {
        isLoadingMore.current = true;
        loadMoreCandles().finally(() => {
          isLoadingMore.current = false;
        });
      }
    });

    chart.subscribeCrosshairMove((param) => {
      if (!param || !param.time || !param.seriesData) { setOhlc(null); return; }
      const bar = param.seriesData.get(candleSeries);
      if (bar) setOhlc(bar);
    });

    const resizeObserver = new ResizeObserver(entries => {
      const { width, height } = entries[0].contentRect;
      chart.resize(width, height);
    });
    resizeObserver.observe(containerRef.current);

    return () => {
      resizeObserver.disconnect();
      chart.remove();
    };
  }, []);

  // ChartComponent.jsx - 데이터 업데이트 useEffect
  useEffect(() => {
    if (!candleSeriesRef.current || !candles || candles.length === 0) return;

    const chartData = candles.map(c => ({
      time: toUnixTime(c.time),
      open: c.open,
      high: c.high,
      low: c.low,
      close: c.close,
    }));

    candleSeriesRef.current.setData(chartData);
    // fitContent는 초기 로드 시에만
    if (!isLoadingMore.current) {
      chartRef.current.timeScale().fitContent();
    }
  }, [candles]);

  return (
    <div className='mnc8st5'>
      <div className='mnc8st6' data-multi-chart-loading='false'>
        <div ref={containerRef} style={{ width: '100%', height: '100%' }}>
          {ohlc && (
            <div style={{ position: 'absolute', top: 8, left: 8, zIndex: 10, fontSize: 12, color: '#8b949e', pointerEvents: 'none' }}>
              시작: {ohlc.open} | 고가: {ohlc.high} | 저가: {ohlc.low} | 종가: {ohlc.close}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}