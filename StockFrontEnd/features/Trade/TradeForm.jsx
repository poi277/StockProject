'use client';

import Trade from "./Trade";

export default function TradeForm({ stockCode, selectedPrice, setSelectedPrice }) {
  const {
    tradeType, setTradeType,
    quantity,  setQuantity,
    message,   error,
    handleSubmit,
  } = Trade(selectedPrice, stockCode);

  const isBuy  = tradeType === "BUY";
  const total  = selectedPrice && quantity
    ? (selectedPrice * Number(quantity)).toLocaleString()
    : "-";

  return (
    <div style={{
      display: "flex", flexDirection: "column",
      height: "100%", boxSizing: "border-box",
    }}>

      {/* 패널 헤더 */}
      <div style={{
        padding: "8px 14px",
        borderBottom: "1px solid #1e2535",
        fontSize: 11, fontWeight: 600, color: "#64748b",
        flexShrink: 0,
      }}>
        주문
      </div>

      {/* 내용 */}
      <div style={{
        flex: 1, padding: 12,
        display: "flex", flexDirection: "column", gap: 10,
        overflowY: "auto",
      }}>

        {/* 매수 / 매도 탭 */}
        <div style={{
          display: "grid", gridTemplateColumns: "1fr 1fr",
          background: "#0e1117", borderRadius: 8, padding: 3, gap: 3,
        }}>
          {["BUY", "SELL"].map(t => (
            <button
              key={t}
              type="button"
              onClick={() => setTradeType(t)}
              style={{
                padding: "8px 0", border: "none", borderRadius: 6,
                cursor: "pointer", fontSize: 13, fontWeight: 700,
                background: tradeType === t
                  ? t === "BUY" ? "#ef5350" : "#3b82f6"
                  : "transparent",
                color: tradeType === t ? "#fff" : "#475569",
                transition: "all 0.15s",
              }}
            >
              {t === "BUY" ? "매수" : "매도"}
            </button>
          ))}
        </div>

        {/* 종목 */}
        <div style={{
          display: "flex", justifyContent: "space-between", alignItems: "center",
          padding: "7px 10px",
          background: "#0e1117", borderRadius: 7,
        }}>
          <span style={{ fontSize: 11, color: "#64748b" }}>종목</span>
          <span style={{ fontSize: 12, fontWeight: 600, color: "#e2e8f0" }}>{stockCode}</span>
        </div>

        {/* 주문가 */}
        <div>
          <label style={{ fontSize: 11, color: "#64748b", display: "block", marginBottom: 4 }}>
            주문 가격
          </label>
          <input
            type="number"
            min="1"
            value={selectedPrice || ""}
            onChange={(e) => setSelectedPrice(Number(e.target.value))}
            placeholder="가격을 입력하세요"
            style={{
              width: "100%", padding: "8px 10px",
              background: "#0e1117",
              border: "1px solid #1e2535",
              borderRadius: 7, color: "#e2e8f0", fontSize: 12,
              outline: "none", boxSizing: "border-box",
            }}
          />
        </div>

        {/* 수량 */}
        <div>
          <label style={{ fontSize: 11, color: "#64748b", display: "block", marginBottom: 4 }}>
            수량
          </label>
          <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
            <button
              type="button"
              onClick={() => setQuantity(q => Math.max(1, Number(q) - 1))}
              style={{
                width: 32, height: 32, flexShrink: 0,
                background: "#1e2535", border: "none",
                borderRadius: 6, color: "#94a3b8",
                cursor: "pointer", fontSize: 16,
              }}
            >−</button>
            <input
              type="number"
              min="1"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              style={{
                flex: 1, padding: "7px 0", textAlign: "center",
                background: "#0e1117", border: "1px solid #1e2535",
                borderRadius: 7, color: "#e2e8f0", fontSize: 12,
                outline: "none",
              }}
            />
            <button
              type="button"
              onClick={() => setQuantity(q => Number(q) + 1)}
              style={{
                width: 32, height: 32, flexShrink: 0,
                background: "#1e2535", border: "none",
                borderRadius: 6, color: "#94a3b8",
                cursor: "pointer", fontSize: 16,
              }}
            >+</button>
          </div>
        </div>

        {/* 총 금액 */}
        <div style={{
          display: "flex", justifyContent: "space-between", alignItems: "center",
          padding: "8px 10px",
          background: "#0e1117", borderRadius: 7,
          borderTop: "1px solid #1e2535",
        }}>
          <span style={{ fontSize: 11, color: "#64748b" }}>총 주문금액</span>
          <span style={{ fontSize: 13, fontWeight: 700, color: "#e2e8f0" }}>
            {total !== "-" ? `${total}원` : "-"}
          </span>
        </div>

        {/* 메시지 */}
        {error && (
          <p style={{
            margin: 0, fontSize: 11, color: "#ef5350",
            background: "rgba(239,83,80,0.08)",
            borderRadius: 6, padding: "6px 10px",
          }}>
            {error}
          </p>
        )}
        {message && (
          <p style={{
            margin: 0, fontSize: 11, color: "#34d399",
            background: "rgba(52,211,153,0.08)",
            borderRadius: 6, padding: "6px 10px",
          }}>
            {message}
          </p>
        )}

        {/* 주문 버튼 */}
        <button
          onClick={handleSubmit}
          style={{
            marginTop: "auto",
            padding: "11px 0", border: "none", borderRadius: 8,
            cursor: "pointer", fontSize: 14, fontWeight: 700,
            background: isBuy ? "#ef5350" : "#3b82f6",
            color: "#fff",
            transition: "opacity 0.15s",
          }}
          onMouseOver={e => e.currentTarget.style.opacity = "0.85"}
          onMouseOut={e => e.currentTarget.style.opacity = "1"}
        >
          {isBuy ? "매수하기" : "매도하기"}
        </button>

      </div>
    </div>
  );
}