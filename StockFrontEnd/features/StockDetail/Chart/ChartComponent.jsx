'use client';

import { useEffect, useRef, useState } from 'react';
import { createChart, ColorType, CandlestickSeries, LineSeries, CrosshairMode } from 'lightweight-charts';
import useCandle from './useChart';
import useChartButtonStore from '../../../store/chartButtonStore';

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

function createMASeries(chart) {
  const ma5 = chart.addSeries(LineSeries, {
    color: '#f6c85f',
    lineWidth: 1,
    priceLineVisible: false,
    lastValueVisible: false,
    crosshairMarkerVisible: false,
  });
  const ma20 = chart.addSeries(LineSeries, {
    color: '#ff7b7b',
    lineWidth: 1,
    priceLineVisible: false,
    lastValueVisible: false,
    crosshairMarkerVisible: false,
  });
  const ma60 = chart.addSeries(LineSeries, {
    color: '#7bc8f6',
    lineWidth: 1,
    priceLineVisible: false,
    lastValueVisible: false,
    crosshairMarkerVisible: false,
  });
  return { ma5, ma20, ma60 };
}

function toUnixTime(timeStr) {
  return Math.floor(new Date(timeStr).getTime() / 1000);
}

// 🎯 차트 타입별 봉 하나의 간격(초)
const INTERVAL_SECONDS = {
  ONE_MINUTE: 60,
  THREE_MINUTE: 180,
  FIVE_MINUTE: 300,
  TEN_MINUTE: 600,
  HOUR: 3600,
  TWO_HOUR: 7200,
  THREE_HOUR: 10800,
  FOUR_HOUR: 14400,
  DAY: 86400,
  WEEK: 86400 * 7,
  MONTH: 86400 * 30,
  YEAR: 86400 * 365,
};

function getIntervalSeconds(type) {
  return INTERVAL_SECONDS[type] ?? 60;
}

// 🎯 lightweight-charts 공식 whitespace data 패턴
// time만 있고 OHLC가 없는 포인트 - 빈 구간에도 시간축이 표시되게 함
function buildWhitespace(lastTime, type, count = 100) {
  const interval = getIntervalSeconds(type);
  const whitespace = [];
  for (let i = 1; i <= count; i++) {
    whitespace.push({ time: lastTime + interval * i });
  }
  return whitespace;
}

function toChartData(candles, type) {
  const realData = candles
    .map(c => ({ time: toUnixTime(c.time), open: c.open, high: c.high, low: c.low, close: c.close }))
    .sort((a, b) => a.time - b.time)
    .filter((c, i, arr) => i === 0 || c.time !== arr[i - 1].time);

  if (realData.length === 0) return realData;

  const last = realData[realData.length - 1];
  const whitespace = buildWhitespace(last.time, type);

  return [...realData, ...whitespace];
}

function toMAData(candles) {
  const sorted = [...candles]
    .sort((a, b) => toUnixTime(a.time) - toUnixTime(b.time))
    .filter((c, i, arr) => i === 0 || toUnixTime(c.time) !== toUnixTime(arr[i - 1].time));
  return {
    ma5: sorted.filter(c => c.movingAverages?.[5] != null).map(c => ({ time: toUnixTime(c.time), value: c.movingAverages[5] })),
    ma20: sorted.filter(c => c.movingAverages?.[20] != null).map(c => ({ time: toUnixTime(c.time), value: c.movingAverages[20] })),
    ma60: sorted.filter(c => c.movingAverages?.[60] != null).map(c => ({ time: toUnixTime(c.time), value: c.movingAverages[60] })),
  };
}

export default function ChartComponent({ stockCode }) {
  const containerRef = useRef(null);
  const chartRef = useRef(null);
  const candleSeriesRef = useRef(null);
  const maSeriesRef = useRef(null);
  const [ohlc, setOhlc] = useState(null);
  const [maValues, setMaValues] = useState(null);
  const isReadyForLoadMore = useRef(false);
  const isLoadingRef = useRef(false);
  const currentMarginRef = useRef({ top: 0.15, bottom: 0.15 });

  // 🎯 setData 호출마다 캡처해두는 전체 데이터 길이(whitespace 포함)
  const totalDataLengthRef = useRef(0);

  // 🎯 현재 선택된 차트 타입 - whitespace 간격을 정확히 계산하기 위해 필요
  const type = useChartButtonStore((state) => state.selectedChartTime);
  const typeRef = useRef(type);
  useEffect(() => {
    typeRef.current = type;
  }, [type]);

  const { datafeedRef, loadMoreCandles, setOnCandleUpdate } = useCandle(stockCode);
  const loadMoreCandlesRef = useRef(loadMoreCandles);

  useEffect(() => {
    loadMoreCandlesRef.current = loadMoreCandles;
  }, [loadMoreCandles]);

  useEffect(() => {
    if (!containerRef.current) return;
    const chart = initChart(containerRef.current);
    const candleSeries = createCandleSeries(chart);
    const maSeries = createMASeries(chart);
    chartRef.current = chart;
    candleSeriesRef.current = candleSeries;
    maSeriesRef.current = maSeries;

    const priceAxisElement = containerRef.current.querySelector('td:last-child');

    const handlePriceScaleWheel = (e) => {
      e.preventDefault();
      e.stopPropagation();
      const zoomFactor = e.deltaY < 0 ? -0.02 : 0.02;
      const newTop = Math.max(0.01, Math.min(0.45, currentMarginRef.current.top + zoomFactor));
      const newBottom = Math.max(0.01, Math.min(0.45, currentMarginRef.current.bottom + zoomFactor));
      currentMarginRef.current = { top: newTop, bottom: newBottom };
      chart.priceScale('right').applyOptions({ scaleMargins: currentMarginRef.current });
    };

    const handleMouseEnter = () => {
      chart.applyOptions({ handleScroll: { mouseWheel: false }, handleScale: { mouseWheel: false } });
    };

    const handleMouseLeave = () => {
      chart.applyOptions({ handleScroll: { mouseWheel: true }, handleScale: { mouseWheel: true } });
    };

    if (priceAxisElement) {
      priceAxisElement.addEventListener('wheel', handlePriceScaleWheel, { passive: false });
      priceAxisElement.addEventListener('mouseenter', handleMouseEnter);
      priceAxisElement.addEventListener('mouseleave', handleMouseLeave);
    }

    setOnCandleUpdate((event) => {
      if (event.type === 'init') {
        const chartData = toChartData(event.candles, typeRef.current);
        const maData = toMAData(event.candles);
        candleSeries.setData(chartData);
        maSeries.ma5.setData(maData.ma5);
        maSeries.ma20.setData(maData.ma20);
        maSeries.ma60.setData(maData.ma60);
        totalDataLengthRef.current = chartData.length;
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
        }, true);
        if (c.movingAverages?.[5] != null) maSeries.ma5.update({ time: toUnixTime(c.time), value: c.movingAverages[5] }, true);
        if (c.movingAverages?.[20] != null) maSeries.ma20.update({ time: toUnixTime(c.time), value: c.movingAverages[20] }, true);
        if (c.movingAverages?.[60] != null) maSeries.ma60.update({ time: toUnixTime(c.time), value: c.movingAverages[60] }, true);

      } else if (event.type === 'completed') {
        const chartData = toChartData(event.candles, typeRef.current);
        const maData = toMAData(event.candles);
        candleSeries.setData(chartData);
        maSeries.ma5.setData(maData.ma5);
        maSeries.ma20.setData(maData.ma20);
        maSeries.ma60.setData(maData.ma60);
        totalDataLengthRef.current = chartData.length;

      } else if (event.type === 'prepend') {
        const chartData = toChartData(event.candles, typeRef.current);
        const maData = toMAData(event.candles);
        const timeScale = chart.timeScale();
        const currentRange = timeScale.getVisibleLogicalRange();

        candleSeries.setData(chartData);
        maSeries.ma5.setData(maData.ma5);
        maSeries.ma20.setData(maData.ma20);
        maSeries.ma60.setData(maData.ma60);
        totalDataLengthRef.current = chartData.length;

        if (currentRange && event.addedCount > 0) {
          timeScale.setVisibleLogicalRange({
            from: currentRange.from + event.addedCount,
            to: currentRange.to + event.addedCount,
          });
        }

        setTimeout(() => {
          isLoadingRef.current = false;
        }, 300);
      }
    });

    chart.timeScale().subscribeVisibleLogicalRangeChange((range) => {
      if (!range) return;

      // 🎯 오른쪽: whitespace 끝(totalDataLengthRef 기준)을 넘어가지 못하게 클램핑
      const maxLogicalIndex = totalDataLengthRef.current - 1;
      if (totalDataLengthRef.current > 0 && range.to > maxLogicalIndex) {
        const overflow = range.to - maxLogicalIndex;
        chart.timeScale().setVisibleLogicalRange({
          from: range.from - overflow,
          to: maxLogicalIndex,
        });
        return;
      }

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
      if (!param || !param.time || !param.seriesData) {
        setOhlc(null);
        setMaValues(null);
        return;
      }
      const bar = param.seriesData.get(candleSeries);
      if (bar) {
        setOhlc(bar);
        const candle = datafeedRef.current.getCandles().find(c => toUnixTime(c.time) === param.time);
        setMaValues(candle?.movingAverages ?? null);
      }
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
      isLoadingRef.current = false;
      setOnCandleUpdate(null);
    };
  }, []);

  return (
    <div className='mnc8st5'>
      <div className='mnc8st6' data-multi-chart-loading='false'>
        <div ref={containerRef} style={{ width: '100%', height: '100%' }}>
          {ohlc && (() => {
            const pct = (val) => (((val - ohlc.open) / ohlc.open) * 100).toFixed(2);
            const sign = (val) => val >= 0 ? `+${val}%` : `${val}%`;
            const fmt = (val) => Math.round(val).toLocaleString();
            return (
              <div style={{ position: 'absolute', top: 8, left: 8, zIndex: 10, fontSize: 12, color: '#8b949e', pointerEvents: 'none' }}>
                <div>
                  시작 {fmt(ohlc.open)} <span>({sign(pct(ohlc.open))})</span>&nbsp;
                  고가 {fmt(ohlc.high)} <span style={{ color: '#fc2d4c' }}>({sign(pct(ohlc.high))})</span>&nbsp;
                  저가 {fmt(ohlc.low)} <span style={{ color: '#007ff3' }}>({sign(pct(ohlc.low))})</span>&nbsp;
                  종가 {fmt(ohlc.close)} <span style={{ color: ohlc.close >= ohlc.open ? '#fc2d4c' : '#007ff3' }}>({sign(pct(ohlc.close))})</span>
                </div>
                {maValues && (
                  <div style={{ marginTop: 2 }}>
                    이동평균선&nbsp;
                    <span style={{ color: '#f6c85f' }}>5 {fmt(maValues[5])}</span>&nbsp;
                    <span style={{ color: '#ff7b7b' }}>20 {fmt(maValues[20])}</span>&nbsp;
                    <span style={{ color: '#7bc8f6' }}>60 {fmt(maValues[60])}</span>
                  </div>
                )}
              </div>
            );
          })()}
        </div>
      </div>
    </div>
  );
}