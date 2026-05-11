import { useEffect, useRef, useState } from 'react';
import useEditStore from '../../../../../store/editStore';
import { editOrderApi } from '../../../../../lib/order';

export default function useSideBarEditOrder() {
    const { editOpen, editTarget, closeEdit } = useEditStore();

    const [price, setPrice] = useState("");
    const popoverRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (
                popoverRef.current &&
                !popoverRef.current.contains(e.target)
            ) {
                closeEdit();
            }
        };

        document.addEventListener("mousedown", handleClickOutside);

        return () => {
            document.removeEventListener(
                "mousedown",
                handleClickOutside
            );
        };
    }, [closeEdit]);

    useEffect(() => {
        if (editTarget) {
            setPrice(String(editTarget.tradePrice));
        }
    }, [editTarget]);

    const handleEditOrder = async () => {
        if (!editTarget) return;

        const response = await editOrderApi(
            editTarget.orderId,
            editTarget.tradeType,
            editTarget.stockCode,
            editTarget.quantity,
            Number(price)
        );

        if (response.success) {
            closeEdit();
        }
    };

    return {
        editOpen,
        editTarget,
        closeEdit,

        price,
        setPrice,

        popoverRef,

        handleEditOrder,
    };
}