import { useState, useEffect } from "react";
import { orderApi } from "../../../../lib/order";

export default function useOrder(selectedPrice, stockCode) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const [priceType, setPriceType] = useState('limit'); // 'limit' | 'market'
    const [price, setPrice] = useState('');
    const [quantity, setQuantity] = useState('');

    // 호가창에서 price 선택 시 input에 반영
    useEffect(() => {
        if (selectedPrice != null) {
            setPrice(selectedPrice.toLocaleString('ko-KR'));
        }
    }, [selectedPrice]);

    async function executeOrder({ tradeType }) {
        setLoading(true);
        setError(null);
        try {
            const numericPrice = priceType === 'market' ? null : Number(price.replace(/,/g, ''));
            const res = await orderApi(tradeType, stockCode, quantity, numericPrice);
            if (!res.success) {
                throw new Error(res.message || "주문 실패");
            }
            return res.data;
        } catch (err) {
            setError(err.message);
            return null;
        } finally {
            setLoading(false);
        }
    }

    return {
        executeOrder,
        loading,
        error,
        priceType, setPriceType,
        price, setPrice,
        quantity, setQuantity,
    };
}