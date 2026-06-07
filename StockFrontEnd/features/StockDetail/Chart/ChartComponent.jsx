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
    timeScale: {
      borderColor: 'rgba(255,255,255,0.05)',
      timeVisible: true,
      secondsVisible: false,
      tickMarkFormatter: (time) => {
        const date = new Date(time * 1000);
        const h = String(date.getHours()).padStart(2, '0');
        const min = String(date.getMinutes()).padStart(2, '0');
        return `${h}:${min}`;
      },
    },
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

function toChartData(candles) {
  return candles
    .map(c => ({ time: toUnixTime(c.time), open: c.open, high: c.high, low: c.low, close: c.close }))
    .sort((a, b) => a.time - b.time)
    .filter((c, i, arr) => i === 0 || c.time !== arr[i - 1].time);
}

export default function ChartComponent({ stockCode, type = 'ONE_MINUTE' }) {
  const containerRef = useRef(null);
  const chartRef = useRef(null);
  const candleSeriesRef = useRef(null);
  const [ohlc, setOhlc] = useState(null);
  const isReadyForLoadMore = useRef(false);
  const isLoadingRef = useRef(false);

  // Y축 휠 줌 스케일 트래킹을 위한 마진 값 기억 Ref
  const currentMarginRef = useRef({ top: 0.15, bottom: 0.15 });

  const { datafeedRef, loadMoreCandles, setOnCandleUpdate } = useCandle(stockCode, type);
  const loadMoreCandlesRef = useRef(loadMoreCandles);

  useEffect(() => {
    loadMoreCandlesRef.current = loadMoreCandles;
  }, [loadMoreCandles]);

  useEffect(() => {
    if (!containerRef.current) return;
    const chart = initChart(containerRef.current);
    const candleSeries = createCandleSeries(chart);
    chartRef.current = chart;
    candleSeriesRef.current = candleSeries;

    // 🎯 [오른쪽 가격 눈금 휠 제어 및 X축 간섭 차단 알고리즘]
    const priceAxisElement = containerRef.current.querySelector('td:last-child');
    
    const handlePriceScaleWheel = (e) => {
      e.preventDefault();
      e.stopPropagation();
      
      const zoomFactor = e.deltaY < 0 ? -0.02 : 0.02; 
      
      const newTop = Math.max(0.01, Math.min(0.45, currentMarginRef.current.top + zoomFactor));
      const newBottom = Math.max(0.01, Math.min(0.45, currentMarginRef.current.bottom + zoomFactor));
      
      currentMarginRef.current = { top: newTop, bottom: newBottom };
      
      chart.priceScale('right').applyOptions({
        scaleMargins: currentMarginRef.current
      });
    };

    // 🎯 마우스가 오른쪽 눈금 영역에 들어오면 X축 휠 옵션을 끈다!
    const handleMouseEnter = () => {
      chart.applyOptions({
        handleScroll: { mouseWheel: false },
        handleScale: { mouseWheel: false }
      });
    };

    // 🎯 마우스가 오른쪽 눈금을 벗어나면 원래대로 다시 켠다!
    const handleMouseLeave = () => {
      chart.applyOptions({
        handleScroll: { mouseWheel: true },
        handleScale: { mouseWheel: true }
      });
    };

    if (priceAxisElement) {
      priceAxisElement.addEventListener('wheel', handlePriceScaleWheel, { passive: false });
      priceAxisElement.addEventListener('mouseenter', handleMouseEnter);
      priceAxisElement.addEventListener('mouseleave', handleMouseLeave);
    }

    setOnCandleUpdate((event) => {
      if (event.type === 'init') {
        const chartData = toChartData(event.candles);
        candleSeries.setData(chartData);
        chart.timeScale().fitContent();
        setTimeout(() => {
          isReadyForLoadMore.current = true;
        }, 500);

      } else if (event.type === 'live') {
        const c = event.candle;
        candleSeries.update({
          time: toUnixTime(c.time),
          open: c.open,
          high: c.high,
          low: c.low,
          close: c.close,
        });

      } else if (event.type === 'prepend') {
        const chartData = toChartData(event.candles);

        const timeScale = chart.timeScale();
        const currentRange = timeScale.getVisibleLogicalRange();
        const oldLength = candleSeriesRef.current ? candleSeriesRef.current.data().length : 0;

        candleSeries.setData(chartData);
        const newLength = chartData.length;
        const addedCount = newLength - oldLength;

        if (currentRange && addedCount > 0) {
          timeScale.setVisibleLogicalRange({
            from: currentRange.from + addedCount,
            to: currentRange.to + addedCount
          });
        }

        setTimeout(() => {
          isLoadingRef.current = false;
        }, 300);
      }
    });

    chart.timeScale().subscribeVisibleLogicalRangeChange((range) => {
      if (!range) return;
      if (!isReadyForLoadMore.current) return;
      if (isLoadingRef.current) return;

      if (range.from < 2) {
        isLoadingRef.current = true;
        loadMoreCandlesRef.current().catch(() => {
          isLoadingRef.current = false;
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
      if (priceAxisElement) {
        priceAxisElement.removeEventListener('wheel', handlePriceScaleWheel);
        priceAxisElement.removeEventListener('mouseenter', handleMouseEnter);
        priceAxisElement.removeEventListener('mouseleave', handleMouseLeave);
      }
      resizeObserver.disconnect();
      chart.remove();
      isReadyForLoadMore.current = false;
      setOnCandleUpdate(null);
    };
  }, []);

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