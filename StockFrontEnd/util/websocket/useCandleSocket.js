import { useEffect, useState } from 'react';

export function useCandleSocket(client, connected, stockCode) {
    const [liveCandle, setLiveCandle] = useState(null); 

    useEffect(() => {
        console.log('useCandleSocket:', { client: !!client, connected, stockCode });
        if (!client || !connected || !stockCode) return;

        const subscription = client.subscribe(`/topic/candle/${stockCode}`, message => {
            const data = JSON.parse(message.body);
            console.log('차트데이터 캔들 RAW:', message.body);
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

    return { liveCandle }; // candles → liveCandle, executions 버그 수정
}