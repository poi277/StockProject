import { useState, useEffect } from "react";
import { orderApi } from "../../../../../lib/order";
 
export default function useOrderSell(selectedPrice, stockCode,stockName,tradeTypeTab,priceType) {

    const [sellQuantity,setSellQuantity] = useState("")
    const [sellPrice,setSellPrice] = useState("")
    // 호가창 price 선택 시 현재 tradeType의 price에 반영 (BUY/SELL만)
    useEffect(() => {
        if (selectedPrice?.value != null && tradeTypeTab === 'SELL') {
        setSellPrice(selectedPrice.value.toLocaleString('ko-KR'));
    }
    }, [selectedPrice]);

    async function sellExcuteOrder({ tradeTypeTab }) {
            try {
                const numericPrice = priceType === 'market' ? null : Number(sellPrice.replace(/,/g, ''));
                const res = await orderApi(tradeTypeTab, stockCode,stockName, sellQuantity, numericPrice);
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
        sellExcuteOrder,
        sellPrice, setSellPrice,
        sellQuantity, setSellQuantity,
    };
}