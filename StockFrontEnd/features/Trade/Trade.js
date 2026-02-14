'use client';

import { useState } from 'react';
import { tradeApi } from '../../lib/trade';

export default function Trade() {
  const [tradeType, setTradeType] = useState('BUY');
  const [stockId, setStockId] = useState('');
  const [quantity, setQuantity] = useState(1);

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');

    if (!stockId || quantity <= 0) {
      setError('모든 값을 올바르게 입력해주세요.');
      return;
    }
    try {
      const res = await tradeApi(tradeType,stockId,quantity)
      if (!res.success) {
        setError(res.message);
        return;
      }
      setMessage(res.message);
    } catch (err) {
      console.error(err);
      setError('거래 요청 중 오류가 발생했습니다.');
    }
  };
  return {
    tradeType,
    stockId,
    quantity,
    message,
    error,
    setTradeType,
    setStockId,
    setQuantity,
    handleSubmit,
  };
}
