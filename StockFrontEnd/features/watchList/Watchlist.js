"use client"

import { useEffect, useState, useMemo } from "react";
import { getWatchListApi } from "../../lib/watchlist";
import { useRouter } from "next/navigation";
import { useStockSocket } from "../../util/useStockSocket";
import { useWebSocket } from "../../util/WebSocket";

export default function useWatchList(initialWatchList) {
    const [watchList, setWatchList] = useState(initialWatchList);
    const router = useRouter();
    const { connected, client } = useWebSocket();

    useEffect(() => {
        fetchWatchList();
    }, []);

    const fetchWatchList = async () => {
        const res = await getWatchListApi();
        console.log(res)
        setWatchList(res.data ?? []);
    };

    const stockCodes = useMemo(() => watchList.map(item => item.stockCode), [watchList]);
    const initialStocks = useMemo(() => Object.fromEntries(watchList.map(item => [item.stockCode, item])), [watchList]);

    const { stocks } = useStockSocket(client, connected, stockCodes, initialStocks);

    // ✅ 소켓 데이터로 watchList 업데이트
    useEffect(() => {
        if (Object.keys(stocks).length === 0) return;
        setWatchList(prev => prev.map(item => ({
            ...item,
            ...stocks[item.stockCode],
        })));
    }, [stocks]);

    const handleClick = (stockCode) => {
        router.push(`/stock/${stockCode}`);
    };

    return { watchList, handleClick };
}