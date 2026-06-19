import { useEffect, useState } from 'react';

export function useCandleSocket(client, connected, stockCode, type) {
    const [liveCandle, setLiveCandle] = useState(null);
    const [completedCandle, setCompletedCandle] = useState(null);

    useEffect(() => {
        if (!client || !connected || !stockCode) return;

        const subscription = client.subscribe(`/topic/candle/${stockCode}`, message => {
            const data = JSON.parse(message.body);
            setLiveCandle({
                open: data.open,
                low: data.low,
                high: data.high,
                close: data.close,
                buyQty: data.buyQty,
                sellQty: data.sellQty,
                time: data.time,
            });
        });

        return () => subscription.unsubscribe();
    }, [client, connected, stockCode]);


    useEffect(() => {
        if (!client || !connected || !stockCode) return;

        const subscription = client.subscribe(`/topic/candle/completed/${stockCode}`, message => {
            const data = JSON.parse(message.body);
            console.log("dddddddddd",data)
            // 🎯 보고 있는 캔들 타입(ONE_MINUTE 등)과 다른 완성봉은 무시
            if (data.candleType && data.candleType !== type) {
                return;
            }

            setCompletedCandle({
                open: data.open,
                low: data.low,
                high: data.high,
                close: data.close,
                buyQty: data.buyQty,
                sellQty: data.sellQty,
                time: data.time,
                candleType: data.candleType,
                movingAverages: data.movingAverages,
            });
        });

        return () => subscription.unsubscribe();
    }, [client, connected, stockCode, type]);

    return { liveCandle, completedCandle };
}