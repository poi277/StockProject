import { useEffect, useState } from "react";
import { useWebSocket } from "../../../util/websocket/context/WebSocketContext";
import { UserHaveAssetContext } from "../../../util/websocket/UserHaveAssetProvider";
import { useStocksSocket } from "../../../util/websocket/useStocksSocket";
import { getStocksByCodesApi } from "../../../lib/stock";

export default function useHaveMyStockAssect() {
    const { haveStocks } = UserHaveAssetContext();
    const { client, connected } = useWebSocket();
    const [initialStocks, setInitialStocks] = useState({});

    const stockCodes = haveStocks?.map(stock => stock.stockCode) ?? [];

    useEffect(() => {
        if (stockCodes.length === 0) return;

        const fetchStockInfo = async () => {
            try {
                const res = await getStocksByCodesApi(stockCodes);
                const stockArray = Array.isArray(res.data) ? res.data : [];
                const initial = Object.fromEntries(
                    stockArray.map(s => [s.stockCode, s])
                );
                setInitialStocks(initial);
            } catch (err) {
                console.error('종목 정보 조회 실패:', err.message);
            }
        };

        fetchStockInfo();
    }, [haveStocks]);

    const { stocks } = useStocksSocket(client, connected, stockCodes, initialStocks);

    const totalInvestment = haveStocks?.reduce((sum, stock) => {
        return sum + (stock.averagePrice * stock.quantity);
    }, 0) ?? 0;

    const SEGMENT_ITEMS = [
        { label: "현재가", value: "left", checked: false, state: "unchecked", activeWeight: "medium", activeColor: "var(--wts-adaptive-greyOpacity600)" },
        { label: "평가금", value: "right", checked: true, state: "checked", activeWeight: "semibold", activeColor: "var(--wts-adaptive-greyOpacity800)" },
    ];

    return { SEGMENT_ITEMS, haveStocks, totalInvestment, stocks };
}