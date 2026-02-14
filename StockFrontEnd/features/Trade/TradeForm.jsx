'use client';

import Trade from "./Trade";

export default function TradeForm() {
  const {
    tradeType,
    setTradeType,
    stockId,
    setStockId,
    quantity,
    setQuantity,
    message,
    error,
    handleSubmit,
  } = Trade();

  return (
    <form onSubmit={handleSubmit}>
      <h2>주식 거래</h2>

      <div>
        <label>거래 유형</label>
        <select
          value={tradeType}
          onChange={(e) => setTradeType(e.target.value)}
        >
          <option value="BUY">매수</option>
          <option value="SELL">매도</option>
        </select>
      </div>
      <div>
        <label>주식 ID</label>
        <input
          value={stockId}
          onChange={(e) => setStockId(e.target.value)}
        />
      </div>

      <div>
        <label>수량</label>
        <input
          type="number"
          min="1"
          value={quantity}
          onChange={(e) => setQuantity(e.target.value)}
        />
      </div>

      {error && <p style={{ color: 'red' }}>{error}</p>}
      {message && <p style={{ color: 'green' }}>{message}</p>}

      <button type="submit">
        {tradeType === 'BUY' ? '매수' : '매도'}
      </button>
    </form>
  );
}
