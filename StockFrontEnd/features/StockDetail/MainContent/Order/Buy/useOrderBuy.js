import { useState, useEffect } from "react";
import { orderApi } from "../../../../../lib/order";
 
export default function useOrderBuy(selectedPrice, stockCode,stockName,tradeTypeTab,priceType,closePrice) {

    const [buyQuantity,setBuyQuantity] = useState("")
    const [buyPrice,setBuyPrice] = useState("")
    // 호가창 price 선택 시 현재 tradeType의 price에 반영 (BUY/SELL만)
    useEffect(() => {
        if (selectedPrice?.value != null && tradeTypeTab === 'BUY') {
            setBuyPrice(selectedPrice.value.toLocaleString('ko-KR'));
        }
    }, [selectedPrice]);

    useEffect(() => {
        if (closePrice) {
            setBuyPrice(closePrice.toLocaleString('ko-KR'));
        }
    }, [closePrice]);

    async function buyExecuteOrder({ tradeTypeTab }) {
            try {
                const numericPrice = priceType === 'market' ? null : Number(buyPrice.replace(/,/g, ''));
                const res = await orderApi(tradeTypeTab, stockCode,stockName, buyQuantity, numericPrice);
                if (!res.success) {
                    throw new Error(res.message || "주문 실패");
                }
                return res.data;
            } catch (err) {
                console.log(err.message);
                return null;
            } 
        }
 
    return {
        buyExecuteOrder,
        buyPrice, setBuyPrice,
        buyQuantity, setBuyQuantity,
    };
}