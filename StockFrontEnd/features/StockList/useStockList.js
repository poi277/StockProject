'use client'

import { useEffect, useState } from "react";
import { useStocksSocket } from "../../util/websocket/useStocksSocket";
import { stockListApi } from "../../lib/stock";
import { useRouter } from "next/navigation";
import { useStockWebSocket } from "../../util/websocket/context/StockWebSocketContext";

export const formatPrice = (price) => price.toLocaleString() + '원'

export const formatChangeRate = (rate) => {
    const fixedRate = rate.toFixed(2);

    if (rate > 0) return `+${fixedRate}%`;
    if (rate < 0) return `${fixedRate}%`;
    return '0.00%';
}

export const formatValue = (value) => {
    if (value >= 100000000) return (value / 100000000).toFixed(0) + '억원'
    if (value >= 10000) return (value / 10000).toFixed(0) + '만원'
    return value.toLocaleString() + '원'
}

export const getChangeColor = (rate) => {
    if (rate > 0) return 'var(--wts-adaptive-red500)'
    if (rate < 0) return 'var(--wts-adaptive-blue500)'
    return 'var(--wts-adaptive-grey600)'
}

export function getTradeRatio(tradeStatus) {
    const buy = tradeStatus?.buyQuantity ?? 0;
    const sell = tradeStatus?.sellQuantity ?? 0;
    const total = buy + sell;
    if (total === 0) return { buyRatio: 50, sellRatio: 50, buyColor: 'var(--wts-adaptive-red600)', sellColor: 'var(--wts-adaptive-blue600)' };
    
    const buyRatio = Math.round((buy / total) * 100);
    const sellRatio = Math.round((sell / total) * 100);
    const buyColor = buyRatio >= sellRatio ? 'var(--wts-adaptive-red600)' : 'var(--wts-adaptive-red100)';
    const sellColor = sellRatio >= buyRatio ? 'var(--wts-adaptive-blue600)' : 'var(--wts-adaptive-blue100)';
    
    return { buyRatio, sellRatio, buyColor, sellColor };
}


export function useStockList() {
    const router = useRouter();
    const { stockConnected, stockClient } = useStockWebSocket();
    const [initialStocks, setInitialStocks] = useState([]);

    useEffect(() => {
        const fetchStocks = async () => {
            try {
                const stocklist = await stockListApi();
                const stockArray = Array.isArray(stocklist.data) ? stocklist.data : [];
                setInitialStocks(stockArray);
            } catch (err) {
                console.error('주식 목록 조회 실패:', err.message);
            }
        };

        fetchStocks();
    }, []);

    const { stocklist } = useStocksSocket(stockClient, stockConnected, initialStocks);

    return { stockConnected, stocklist, router };
}