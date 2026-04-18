import { useEffect, useMemo, useState } from "react";
import { getOrderbookApi } from "../../../../lib/trade";

export default function useHoga(stockCode) {

    const [sellOrders, setSellOrders] = useState([]);
    const [buyOrders, setBuyOrders] = useState([]);
    const totalSellQuantity = useMemo(() =>
        sellOrders.reduce((sum, o) => sum + o.quantity, 0)
    , [sellOrders]);

    const totalBuyQuantity = useMemo(() =>
        buyOrders.reduce((sum, o) => sum + o.quantity, 0)
    , [buyOrders]);

    async function fetchOrderbook() {
        try {
            const res = await getOrderbookApi(stockCode);
            const data = res.data;

            setSellOrders(data.sellOrders.map(o => ({
                price: o.tradePrice,
                quantity: o.remainingQuantity,
            })));

            setBuyOrders(data.buyOrders.map(o => ({
                price: o.tradePrice,
                quantity: o.remainingQuantity,
            })));
        } catch (error) {
            console.error("호가 조회 실패:", error);
        }
    }

    useEffect(() => {
    if (!stockCode) return;
    fetchOrderbook();
    }, [stockCode]);

    const maxQuantity = useMemo(() => {
        const all = [...sellOrders, ...buyOrders];
        if (all.length === 0) return 1;
        return Math.max(...all.map(o => o.quantity));
    }, [sellOrders, buyOrders]);

    const getBarWidth = (quantity) =>
        `calc(${(quantity / maxQuantity) * 100}% - 8px)`;

    return {
        sellOrders, setSellOrders,
        buyOrders, setBuyOrders,
        maxQuantity, getBarWidth,
        totalSellQuantity, totalBuyQuantity
    };
}