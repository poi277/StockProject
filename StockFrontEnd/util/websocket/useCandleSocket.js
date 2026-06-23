import { useEffect, useState } from 'react';

const MINUTE_BASED_TYPES = new Set([
    'ONE_MINUTE',
    'THREE_MINUTE',
    'FIVE_MINUTE',
    'TEN_MINUTE',
    'HOUR',
    'TWO_HOUR',
    'THREE_HOUR',
    'FOUR_HOUR',
]);

function getSubscribeType(type) {
    if (MINUTE_BASED_TYPES.has(type)) return 'ONE_MINUTE';
    if (type === 'DAY') return 'DAY';
    return type;
}

function toCandlePayload(data) {
    return {
        open: data.open,
        low: data.low,
        high: data.high,
        close: data.close,
        buyQty: data.buyQty,
        sellQty: data.sellQty,
        time: data.time,
        candleType: data.candleType,
        movingAverages: data.movingAverages,
    };
}

export function useCandleSocket(client, connected, stockCode, type) {
    const [liveCandle, setLiveCandle] = useState(null);
    const [completedCandle, setCompletedCandle] = useState(null);

    const subscribeType = getSubscribeType(type);

    useEffect(() => {
        if (!client || !connected || !stockCode || !subscribeType) return;

        const subscription = client.subscribe(`/topic/candle/${stockCode}/${subscribeType}`, message => {
            setLiveCandle(toCandlePayload(JSON.parse(message.body)));
        });

        return () => subscription.unsubscribe();
    }, [client, connected, stockCode, subscribeType]);

    useEffect(() => {
        if (!client || !connected || !stockCode || !subscribeType) return;

        const subscription = client.subscribe(`/topic/candle/completed/${stockCode}/${subscribeType}`, message => {
            setCompletedCandle(toCandlePayload(JSON.parse(message.body)));
        });

        return () => subscription.unsubscribe();
    }, [client, connected, stockCode, subscribeType]);

    return { liveCandle, completedCandle };
}