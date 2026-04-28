import { useState, useEffect } from "react";
import { editOrderApi } from "../../../../../lib/order";

export default function useOrderEdit(selectedPrice, stockCode,tradeTypeTab) {
    // PENDING 수정 상태 — 닫으면 null로 초기화
    const [edit, setEdit] = useState(false);
    const [editTarget, setEditTarget] = useState(null);
    // 수정폼 내부 state — 열릴 때 초기값 세팅, 닫으면 초기화
    const [editPrice, setEditPrice] = useState('');
    const [editQuantity, setEditQuantity] = useState('');
    const [editPriceType, setEditPriceType] = useState('limit');

    // 호가창 price 선택 시 현재 tradeType의 price에 반영 (BUY/SELL만)
    useEffect(() => {
        if (selectedPrice != null && tradeTypeTab=='PENDING' && edit==true) {
            setBuyPrice(editPrice.toLocaleString('ko-KR'));
        }
    }, [selectedPrice]);

    // 수정 열기 — 초기값 세팅
    const handleEditOpen = (order) => {
        setEditTarget(order);
        setEditPrice(order.price.toLocaleString('ko-KR'));
        setEditQuantity(String(order.quantity));
        setEditPriceType('limit');
        setEdit(true);
    };

    // 수정 닫기 — 초기화
    const handleEditClose = () => {
        setEditTarget(null);
        setEditPrice('');
        setEditQuantity('');
        setEditPriceType('limit');
        setEdit(false);
    };

    async function executeOrder({}) {
            try {
                const numericPrice = editPriceType === 'market' ? null : Number(price.replace(/,/g, ''));
                const res = await editOrderApi(editTarget.id,editTarget.tradeType, stockCode, editQuantity, numericPrice,);
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
        edit,setEdit,
        editTarget,setEditTarget,
        editPrice, setEditPrice,
        editQuantity, setEditQuantity,
        editPriceType, setEditPriceType,
        handleEditOpen,
        handleEditClose,
    };
}