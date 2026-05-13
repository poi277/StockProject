'use client'

import { useEffect, useState } from "react";
import { useStocksSocket } from "../../util/websocket/useStocksSocket";
import { useWebSocket } from "../../util/websocket/context/WebSocketContext";
import { stockListApi } from "../../lib/stock";
import { useRouter } from "next/navigation";

export function useStockList() {
    const router = useRouter();
    const { connected, client } = useWebSocket();
    const [stockCodes, setStockCodes] = useState([]);
    const [initialStocks, setInitialStocks] = useState({});

    useEffect(() => {
        const fetchStocks = async () => {
            try {
                const stocklist = await stockListApi();
                const stockArray = Array.isArray(stocklist.data) ? stocklist.data : [];

                // stockCode 배열 추출
                const codes = stockArray.map(stock => stock.stockCode);
                setStockCodes(codes);

                // 초기 stocks 객체 생성
                const initial = Object.fromEntries(
                    stockArray.map(s => [s.stockCode, s])
                );
                setInitialStocks(initial);
            } catch (err) {
                console.error('주식 목록 조회 실패:', err.message);
            }
        };

        fetchStocks();
    }, []);

    const { stocks } = useStocksSocket(client, connected, stockCodes, initialStocks);

    return {
        connected,
        stocks,
        router
    };
}