'use client'

import { useEffect, useCallback, useRef } from "react";
import { getCandleApi, getCandleInitApi } from "../../../lib/candle";
import { useCandleSocket } from "../../../util/websocket/useCandleSocket";
import { useOrderWebSocket } from "../../../util/websocket/context/OrderWebSocketContext";
import useChartButtonStore from "../../../store/chartButtonStore";

const MA_PERIODS = [5, 20, 60];

function calculateLiveMA(confirmedCandles, currentCandle) {
    const ma = {};
    const closes = confirmedCandles.map(c => c.close);

    for (const period of MA_PERIODS) {
        const recentCloses = closes.slice(-(period - 1));
        const allCloses = [...recentCloses, currentCandle.close];
        const sum = allCloses.reduce((a, b) => a + b, 0);
        ma[period] = Math.round((sum / allCloses.length) * 100) / 100;
    }

    return ma;
}

function hasFullMA(candle) {
    return !!candle.movingAverages
        && Object.keys(candle.movingAverages).length > 0
        && MA_PERIODS.every(p => candle.movingAverages[p] != null);
}

function enrichLastCandleMA(candles) {
    if (!candles || candles.length === 0) return candles;

    const last = candles[candles.length - 1];
    if (hasFullMA(last)) return candles;

    const confirmedCandles = candles.slice(0, -1);
    const liveMA = calculateLiveMA(confirmedCandles, last);
    const enrichedLast = { ...last, movingAverages: liveMA };

    return [...confirmedCandles, enrichedLast];
}
function normalizeCandleTime(timeStr, candleType) {
    const date = new Date(timeStr);

    // KST(UTC+9) 기준으로 시/분 추출
    const kstOffset = 9 * 60;
    const kstDate = new Date(date.getTime() + kstOffset * 60 * 1000);

    const y = kstDate.getUTCFullYear();
    const m = String(kstDate.getUTCMonth() + 1).padStart(2, '0');
    const d = String(kstDate.getUTCDate()).padStart(2, '0');

    let hours = kstDate.getUTCHours();
    let minutes = kstDate.getUTCMinutes();

    switch (candleType) {
        case 'THREE_MINUTE':
            minutes = Math.floor(minutes / 3) * 3;
            break;
        case 'FIVE_MINUTE':
            minutes = Math.floor(minutes / 5) * 5;
            break;
        case 'TEN_MINUTE':
            minutes = Math.floor(minutes / 10) * 10;
            break;
        case 'HOUR':
            minutes = 0;
            break;
        case 'TWO_HOUR':
            hours = Math.floor(hours / 2) * 2;
            minutes = 0;
            break;
        case 'THREE_HOUR':
            hours = Math.floor(hours / 3) * 3;
            minutes = 0;
            break;
        case 'FOUR_HOUR':
            hours = Math.floor(hours / 4) * 4;
            minutes = 0;
            break;

        // 🎯 [여기만 직관적으로 명확하게 수정]
        case 'DAY':
            return `${y}-${m}-${d}`; // 일봉은 오늘 날짜 그대로 (예: 2026-06-25)
            
        case 'WEEK':
            // 💡 월요일로 강제 매핑 (0:일, 1:월, 2:화, 3:수, 4:목, 5:금, 6:토)
            const dayIdx = kstDate.getUTCDay();
            const diffToMonday = dayIdx === 0 ? -6 : 1 - dayIdx; // 일요일이면 6일 전, 평지면 월요일과의 차이 계산
            
            const monday = new Date(kstDate.getTime() + diffToMonday * 24 * 60 * 60 * 1000);
            const wy = monday.getUTCFullYear();
            const wm = String(monday.getUTCMonth() + 1).padStart(2, '0');
            const wd = String(monday.getUTCDate()).padStart(2, '0');
            return `${wy}-${wm}-${wd}`; // 주봉은 해당 주의 월요일 날짜 반환
            
        case 'MONTH':
            return `${y}-${m}-01`;   // 월봉은 무조건 해당 월의 1일로 매핑 (예: 2026-06-01)
            
        case 'YEAR':
            return `${y}-01-01`;     // 년봉은 무조건 해당 년의 1월 1일로 매핑 (예: 2026-01-01)

        default:
            break;
    }

    const h = String(hours).padStart(2, '0');
    const min = String(minutes).padStart(2, '0');

    return `${y}-${m}-${d}T${h}:${min}`;
}

class Datafeed {
    constructor() {
        this._candles = [];
        this._earliestTime = null;
        this._isLoading = false;
    }

    setInitialData(candles) {
        this._candles = [...candles];
        if (candles.length > 0) {
            this._earliestTime = new Date(candles[0].time);
        }
    }

    async loadMore(stockCode, type, fetchFn) {
        if (!this._earliestTime) return this._candles;
        if (this._isLoading) return this._candles;

        this._isLoading = true;

        // 🎯 기존 가장 오래된 캔들 시점을 기준으로 새로운 종료(endTime)와 시작(startTime)을 계산합니다.
        const endTime = new Date(this._earliestTime);
        const startTime = new Date(this._earliestTime);

        // ==========================================
        // 1️⃣ endTime 차감 로직 (중복 데이터 방지용 -1칸 절삭)
        // ==========================================
        if (['ONE_MINUTE', 'THREE_MINUTE', 'FIVE_MINUTE', 'TEN_MINUTE'].includes(type)) {
            endTime.setMinutes(endTime.getMinutes() - 1); // 현재 분봉에서 -1분
        } 
        else if (['HOUR', 'TWO_HOUR', 'THREE_HOUR', 'FOUR_HOUR'].includes(type)) {
            endTime.setMinutes(endTime.getMinutes() - 60); // 시봉 계열은 -60분 (1시간)
        } 
        else if (type === 'DAY' || type === 'WEEK') {
            endTime.setDate(endTime.getDate() - 1); // 일봉, 주봉은 -1일
        } 
        else if (type === 'MONTH') {
            endTime.setMonth(endTime.getMonth() - 1); // 월봉은 -1달
        } 
        else if (type === 'YEAR') {
            endTime.setFullYear(endTime.getFullYear() - 1); // 년봉은 -1년
        }

        // ==========================================
        // 2️⃣ startTime 차감 로직 (과거 데이터 호출 범위 지정)
        // ==========================================
        // 감소된 endTime을 기준점으로 삼아 과거 범위를 잡아야 기간이 꼬이지 않습니다.
        startTime.setTime(endTime.getTime()); 

        if (type === 'DAY' || type === 'WEEK') {
            startTime.setDate(startTime.getDate() - 30);      // 일/주봉은 과거 30일치씩
        } else if (type === 'MONTH') {
            startTime.setMonth(startTime.getMonth() - 12);    // 월봉은 과거 1년(12달)치씩
        } else if (type === 'YEAR') {
            startTime.setFullYear(startTime.getFullYear() - 10); // 년봉은 과거 10년치씩
        } else {
            startTime.setMinutes(startTime.getMinutes() - 120); // 분/시봉은 과거 120분치씩
        }
        
        // 다음 스크롤을 위해 차트의 가장 최과거 시점을 업데이트
        this._earliestTime = startTime;

        try {
            const res = await fetchFn(stockCode, type, startTime, endTime);

            if (res.data?.length) {
                const enrichedData = enrichLastCandleMA(res.data);
                this._candles = [...enrichedData, ...this._candles];
            }
        } catch (err) {
            console.error(err);
            // 에러 발생 시 원래 시점으로 롤백하여 재시도할 수 있도록 처리
            this._earliestTime = endTime; 
        } finally {
            this._isLoading = false;
        }

        return this._candles;
    }

    getCandles() {
        return this._candles;
    }

    addCompletedCandle(completedCandle, candleType) {
        const normalizedTime = normalizeCandleTime(completedCandle.time, candleType);

        const normalizedCandle = {
            ...completedCandle,
            time: normalizedTime,
        };

        const candles = this._candles;

        const toUnix = (timeStr) => Math.floor(new Date(timeStr).getTime() / 1000);
        const targetUnix = toUnix(normalizedCandle.time);

        const idx = candles.findIndex(c => toUnix(c.time) === targetUnix);

        if (idx === -1) {
            this._candles = [...candles, normalizedCandle];
            return;
        }

        const prev = candles[idx];

        const mergedCandle = {
            ...prev,
            high: Math.max(prev.high, normalizedCandle.high),
            low: Math.min(prev.low, normalizedCandle.low),
            close: normalizedCandle.close,
            buyQty: (prev.buyQty ?? 0) + (normalizedCandle.buyQty ?? 0),
            sellQty: (prev.sellQty ?? 0) + (normalizedCandle.sellQty ?? 0),
            movingAverages: normalizedCandle.movingAverages ?? prev.movingAverages,
        };

        this._candles = [
            ...candles.slice(0, idx),
            mergedCandle,
            ...candles.slice(idx + 1),
        ];
    }

    addLiveCandle(liveCandle, candleType) {

        const normalizedTime =
            normalizeCandleTime(
                liveCandle.time,
                candleType
            );

        const normalizedCandle = {
            ...liveCandle,
            time: normalizedTime,
        };

        const candles = this._candles;

        const toUnix = (timeStr) =>
            Math.floor(
                new Date(timeStr).getTime() / 1000
            );

        const confirmedCandles =
            candles.length > 0 &&
                toUnix(candles[candles.length - 1].time)
                === toUnix(normalizedCandle.time)
                ? candles.slice(0, -1)
                : candles;

        const liveMA =
            calculateLiveMA(
                confirmedCandles,
                normalizedCandle
            );

        const enrichedCandle = {
            ...normalizedCandle,
            movingAverages: liveMA,
        };

        if (candles.length === 0) {
            this._candles = [enrichedCandle];
            return;
        }

        const last = candles[candles.length - 1];

        if (
            toUnix(last.time)
            === toUnix(normalizedCandle.time)
        ) {
            this._candles = [
                ...candles.slice(0, -1),
                enrichedCandle,
            ];
        } else {
            this._candles = [
                ...candles,
                enrichedCandle,
            ];
        }
    }
}

export default function useCandle(stockCode) {
    const type = useChartButtonStore((state) => state.selectedChartTime);

    const datafeedRef = useRef(new Datafeed());
    const onCandleUpdateRef = useRef(null);
    const { client, connected } = useOrderWebSocket();
    const { liveCandle, completedCandle } = useCandleSocket(client, connected, stockCode, type);

    const setOnCandleUpdate = useCallback((cb) => {
        onCandleUpdateRef.current = cb;
    }, []);

    const fetchInitialCandles = useCallback(async () => {
        if (!stockCode || !type) return;
        datafeedRef.current = new Datafeed();
        try {
            const res = await getCandleInitApi(stockCode, type);
            const enrichedCandles = enrichLastCandleMA(res.data);
            datafeedRef.current.setInitialData(enrichedCandles);
            onCandleUpdateRef.current?.({ type: 'init', candles: enrichedCandles });
        } catch (err) {
            console.error(err);
        }
    }, [stockCode, type]);

    useEffect(() => {
        fetchInitialCandles();
    }, [fetchInitialCandles]);

    useEffect(() => {
        if (!completedCandle) return;

        datafeedRef.current.addCompletedCandle(completedCandle, type);

        onCandleUpdateRef.current?.({
            type: 'completed',
            chartType: type,
        });
    }, [completedCandle, type]);

    useEffect(() => {
        if (!liveCandle) return;

        datafeedRef.current.addLiveCandle(liveCandle, type);

        onCandleUpdateRef.current?.({
            type: 'live',
            chartType: type,
        });
    }, [liveCandle, type]);

    const loadMoreCandles = useCallback(async () => {
        const before = datafeedRef.current.getCandles().length; // 🎯 호출 전 순수 캔들 개수
        const allCandles = await datafeedRef.current.loadMore(stockCode, type, getCandleApi);
        const addedCount = allCandles.length - before; // 🎯 실제로 추가된 과거 캔들 개수
        onCandleUpdateRef.current?.({ type: 'prepend', candles: allCandles, addedCount });
        return allCandles;
    }, [stockCode, type]);

    return { datafeedRef, loadMoreCandles, setOnCandleUpdate };
}