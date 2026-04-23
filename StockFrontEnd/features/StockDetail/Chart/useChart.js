'use client'

import { useEffect, useState, useCallback, useRef } from "react";
import { getCandleApi } from "../../../lib/candle";
import { useCandleSocket } from "../../../util/useCandleSocket";
import { useWebSocket } from "../../../util/WebSocket";

function toUnixTime(timeStr) {
    return Math.floor(new Date(timeStr).getTime() / 1000);
}

export default function useCandle(stockCode, type = "ONE_MINUTE") {
    const [candles, setCandles] = useState([]);
    const { client, connected } = useWebSocket();
    const { liveCandle } = useCandleSocket(client, connected, stockCode);

    const timeRangeRef = useRef({
        startTime: (() => {
            const t = new Date();
            t.setMinutes(t.getMinutes() - 160);
            return t;
        })(),
        endTime: new Date(),
    });

    useEffect(() => {
        if (!stockCode) return;
        const fetchCandle = async () => {
            try {
                const res = await getCandleApi(stockCode, type, timeRangeRef.current.startTime, timeRangeRef.current.endTime);
                setCandles(res.data);
            } catch (err) {
                console.error(err);
            }
        };
        fetchCandle();
    }, [stockCode, type]);

    useEffect(() => {
        if (!liveCandle) return;
        setCandles(prev => {
            if (prev.length === 0) return [liveCandle];
            const last = prev[prev.length - 1];
            if (toUnixTime(last.time) === toUnixTime(liveCandle.time)) {
                return [...prev.slice(0, -1), liveCandle];
            }
            return [...prev, liveCandle];
        });
    }, [liveCandle]);

    const loadMoreCandles = useCallback(async () => {
        const newEndTime = timeRangeRef.current.startTime;
        const newStartTime = new Date(timeRangeRef.current.startTime);
        newStartTime.setMinutes(newStartTime.getMinutes() - 60);
        try {
            const res = await getCandleApi(stockCode, type, newStartTime, newEndTime);
            if (res.data?.length) {
                timeRangeRef.current.startTime = newStartTime;
                setCandles(prev => [...res.data, ...prev]);
            }
        } catch (err) {
            console.error(err);
        }
    }, [stockCode, type]);

    return { candles, loadMoreCandles };
}