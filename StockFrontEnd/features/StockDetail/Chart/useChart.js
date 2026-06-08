'use client'

import { useEffect, useCallback, useRef } from "react";
import { getCandleApi,getCandleInitApi } from "../../../lib/candle";
import { useCandleSocket } from "../../../util/websocket/useCandleSocket";
import { useOrderWebSocket } from "../../../util/websocket/context/OrderWebSocketContext";

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
        const endTime = new Date(this._earliestTime);
        const startTime = new Date(this._earliestTime);
        
        // 🎯 [서버 부하 경감 최적화] 드래그 시 청크 단위로 120분(2시간) 치 데이터를 한 번에 요청
        startTime.setMinutes(startTime.getMinutes() - 120);
        this._earliestTime = startTime;

        try {
            const res = await fetchFn(stockCode, type, startTime, endTime);
            if (res.data?.length) {
                // 가져온 과거 데이터를 배열 맨 앞에 결합
                this._candles = [...res.data, ...this._candles];
            }
        } catch (err) {
            console.error(err);
            this._earliestTime = endTime; // 실패 시 타임라인 롤백 방어
        } finally {
            this._isLoading = false;
        }

        return this._candles;
    }

    getCandles() {
        return this._candles;
    }

    addLiveCandle(liveCandle) {
        const candles = this._candles;
        if (candles.length === 0) return;
        const last = candles[candles.length - 1];
        
        // 시간 비교를 위해 Unix 타임스탬프로 변환 후 마지막 캔들 업데이트 또는 신규 추가
        const toUnix = (timeStr) => Math.floor(new Date(timeStr).getTime() / 1000);
        
        if (toUnix(last.time) === toUnix(liveCandle.time)) {
            this._candles = [...candles.slice(0, -1), liveCandle];
        } else {
            this._candles = [...candles, liveCandle];
        }
    }
}

export default function useCandle(stockCode, type = "ONE_MINUTE") {
    const datafeedRef = useRef(new Datafeed());
    const onCandleUpdateRef = useRef(null);
    const { client, connected } = useOrderWebSocket();
    const { liveCandle } = useCandleSocket(client, connected, stockCode);

    const setOnCandleUpdate = useCallback((cb) => {
        onCandleUpdateRef.current = cb;
    }, []);

    const fetchInitialCandles = useCallback(async () => {
        if (!stockCode) return;
        datafeedRef.current = new Datafeed();
        try {
            const endTime = new Date();
            const startTime = new Date();
            startTime.setMinutes(startTime.getMinutes() - 600);
            const res = await getCandleInitApi(stockCode, type);
            console.log(res)
            datafeedRef.current.setInitialData(res.data);
            onCandleUpdateRef.current?.({ type: 'init', candles: res.data });
        } catch (err) {
            console.error(err);
        }
    }, [stockCode, type]);

    useEffect(() => {
        fetchInitialCandles();
    }, [fetchInitialCandles]);

    useEffect(() => {
        if (!liveCandle) return;
        datafeedRef.current.addLiveCandle(liveCandle);
        onCandleUpdateRef.current?.({ type: 'live', candle: liveCandle });
    }, [liveCandle]);

    // 🎯 외부(Component)에서 프로미스 제어를 할 수 있도록 async/await 흐름 보장 및 데이터 반환
        const loadMoreCandles = useCallback(async () => {
        const allCandles = await datafeedRef.current.loadMore(stockCode, type, getCandleApi);
        onCandleUpdateRef.current?.({ type: 'prepend', candles: allCandles });
        return allCandles;
    }, [stockCode, type]);

    return { datafeedRef, loadMoreCandles, setOnCandleUpdate };
}