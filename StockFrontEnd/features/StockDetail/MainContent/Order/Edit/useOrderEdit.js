import { useState, useEffect } from "react";
import { editOrderApi } from "../../../../../lib/order"; 
import { useAuth } from "../../../../../context/AuthContext";
import { useOrder } from "../../../../../util/OrderSocket";

export default function useOrderEdit(selectedPrice, stockCode, tradeTypeTab) {

    const { user } = useAuth();
    const { orders=[], setOrders } = useOrder();

    const [edit, setEdit] = useState(false);
    const [editTarget, setEditTarget] = useState(null);
    const [editPrice, setEditPrice] = useState('');
    const [editQuantity, setEditQuantity] = useState('');
    const [editPriceType, setEditPriceType] = useState('limit');
    const [error, setError] = useState(null); 
    const [loading, setLoading] = useState(false);

    const handleEditOpen = (order) => {
        setEditTarget(order);
        setEditPrice(order.tradePrice.toLocaleString('ko-KR'));
        setEditQuantity(String(order.quantity));
        setEditPriceType('limit');
        setEdit(true);
    };

    const handleEditClose = () => {
        setEditTarget(null);
        setEditPrice('');
        setEditQuantity('');
        setEditPriceType('limit');
        setEdit(false);
    };

    // ✅ 파라미터 {} 제거, price → editPrice, async function → const
    const editExecuteOrder = async () => {
        setLoading(true);
        setError(null);
        try {
            const numericPrice = editPriceType === 'market'
                ? null
                : Number(editPrice.replace(/,/g, ''));
            const res = await editOrderApi(
                editTarget.id,
                editTarget.tradeType,
                stockCode,
                editQuantity,
                numericPrice,
            );
            if (!res.success) throw new Error(res.message || "주문 실패");

            // // ✅ 낙관적 업데이트 (웹소켓 오기 전에 UI 먼저 반영)
            // setOrders(prev => prev.map(o =>
            //     o.orderId === editTarget.id
            //         ? { ...o, price: numericPrice, quantity: Number(editQuantity) }
            //         : o
            // ));
            handleEditClose();
            return res.data;
        } catch (err) {
            setError(err.message);
            return null;
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (selectedPrice != null && tradeTypeTab === 'PENDING' && edit === true) {
            setEditPrice(selectedPrice.toLocaleString('ko-KR'));
        }
    }, [selectedPrice]);

    const stockOrders = orders.filter(o => o.stockCode === stockCode);

    return {
        editExecuteOrder,
        edit, setEdit,
        editTarget, setEditTarget,
        editPrice, setEditPrice,
        editQuantity, setEditQuantity,
        editPriceType, setEditPriceType,
        handleEditOpen,
        handleEditClose,
        stockOrders, // ✅ 전체 orders 대신 현재 종목 것만
        error,
        loading,
    };
}