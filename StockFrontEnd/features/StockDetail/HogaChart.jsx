'use client';

import { useState, useEffect } from 'react';
import { getOrderbookApi } from '../../lib/trade';
import { useHogaSocket } from '../../util/useHogaSocket';
import { useWebSocket } from '../../util/WebSocketContext';
import { useExecutionSocket } from '../../util/useExecutionSocket';

export default function HogaChart({ currentStock, selectedPrice, setSelectedPrice }) {
  const { connected, client } = useWebSocket();
  const [initialSell, setInitialSell] = useState([]);
  const [initialBuy,  setInitialBuy]  = useState([]);

  const openPrice  = currentStock?.openPrice   || 0;
  const closePrice = currentStock?.closePrice  || 0;
  const changeAmt  = currentStock?.changeAmount || 0;
  const changeRate = currentStock?.changeRate   || 0;
  const isUp       = changeAmt >= 0;

  useEffect(() => {
    if (!currentStock?.stockCode) return;
    getOrderbookApi(currentStock.stockCode)
      .then(res => {
        const sellDb = res.data?.sellOrders || [];
        const buyDb  = res.data?.buyOrders  || [];
        setInitialSell(sellDb.map(o => ({ price: o.tradePrice, qty: o.remainingQuantity })));
        setInitialBuy(buyDb.map(o =>  ({ price: o.tradePrice, qty: o.remainingQuantity })));
      })
      .catch(() => { setInitialSell([]); setInitialBuy([]); });
  }, [currentStock?.stockCode]);

  const { sellOrders, buyOrders } = useHogaSocket(
    client, connected, currentStock?.stockCode, initialSell, initialBuy
  );
  const { executions } = useExecutionSocket(client, connected, currentStock?.stockCode);

  const fmt = (n) => Number(n).toLocaleString('ko-KR');

  const priceColor = (price) => {
    if (!openPrice) return '#94a3b8';
    if (price > openPrice) return '#ef5350';
    if (price < openPrice) return '#3b82f6';
    return '#94a3b8';
  };

  const pct = (price) => {
    if (!openPrice) return '0.00%';
    const v = ((price - openPrice) / openPrice * 100).toFixed(2);
    return (v > 0 ? '+' : '') + v + '%';
  };

  const displaySell = [...sellOrders].slice(0, 5).reverse();
  const displayBuy  = [...buyOrders].slice(0, 5);
  const allQtys     = [...displaySell, ...displayBuy].map(o => o.qty);
  const maxQty      = Math.max(...allQtys, 1);
  const recentEx    = executions.slice(0, 5);

  const ROW_H = 36;

  return (
     <div style={{
        display: "flex",
        flexDirection: "column",
        height: "100%",
        background: "transparent",
        fontSize: 12,
        userSelect: "none",
      }}>

      {/* 패널 헤더 */}
      <div style={{
        padding: "10px 14px",
        borderBottom: "1px solid #1e2535",
        fontSize: 12, fontWeight: 600, color: "#64748b", letterSpacing: "0.05em",
      }}>
        호가
      </div>

      {/* 컬럼 헤더 */}
      <div style={{
        display: "grid", gridTemplateColumns: "1fr 90px 1fr",
        padding: "4px 8px",
        fontSize: 10, color: "#475569",
        borderBottom: "1px solid #1e2535",
        background: "#0e1117",
      }}>
        <span style={{ textAlign: "right" }}>잔량</span>
        <span style={{ textAlign: "center" }}>가격</span>
        <span style={{ textAlign: "left" }}>잔량</span>
      </div>

      {/* ── 매도 호가 5줄 ── */}
      {displaySell.map((o, i) => {
        const barPct     = Math.round((o.qty / maxQty) * 100);
        const isSelected = selectedPrice === o.price;
        return (
          <div
            key={`sell-${i}`}
            onClick={() => setSelectedPrice(o.price)}
            style={{
              display: "grid", gridTemplateColumns: "1fr 90px 1fr",
              alignItems: "center",
              height: ROW_H,
              padding: "0 8px",
              cursor: "pointer",
              background: isSelected ? "rgba(239,83,80,0.08)" : "transparent",
              borderBottom: "1px solid #131926",
            }}
          >
            {/* 잔량 + 바 (오른쪽 정렬) */}
            <div style={{
              position: "relative", textAlign: "right",
              overflow: "hidden", height: "100%",
              display: "flex", alignItems: "center", justifyContent: "flex-end",
            }}>
              <div style={{
                position: "absolute", top: 0, right: 0, bottom: 0,
                width: `${barPct}%`,
                background: "rgba(239,83,80,0.13)",
              }} />
              <span style={{ position: "relative", color: "#94a3b8", fontSize: 11 }}>
                {fmt(o.qty)}
              </span>
            </div>

            {/* 가격 + % */}
            <div style={{ textAlign: "center" }}>
              <div style={{ fontWeight: 600, fontSize: 12, color: priceColor(o.price) }}>
                {fmt(o.price)}
              </div>
              <div style={{ fontSize: 10, color: "#475569", marginTop: 1 }}>
                {pct(o.price)}
              </div>
            </div>

            {/* 빈 칸 */}
            <div />
          </div>
        );
      })}



      {/* ── 매수 호가 5줄 + 왼쪽 체결내역 ── */}
      {displayBuy.map((o, i) => {
        const barPct     = Math.round((o.qty / maxQty) * 100);
        const isSelected = selectedPrice === o.price;
        const ex         = recentEx[i];

        return (
          <div
            key={`buy-${i}`}
            style={{
              display: "grid", gridTemplateColumns: "1fr 90px 1fr",
              alignItems: "center",
              height: ROW_H,
              padding: "0 8px",
              borderBottom: "1px solid #131926",
            }}
          >
            {/* 왼쪽: 체결 내역 */}
            <div style={{ display: "flex", flexDirection: "column", gap: 1, paddingRight: 4 }}>
              {ex && (
                <>
                  <span style={{
                    fontSize: 11, fontWeight: 600,
                    color: ex.tradeType === 'BUY' ? '#ef5350' : '#3b82f6',
                  }}>
                    {fmt(ex.price)}
                  </span>
                  <span style={{ fontSize: 10, color: "#475569" }}>
                    {fmt(ex.quantity)}
                  </span>
                </>
              )}
            </div>

            {/* 가격 + % */}
            <div
              onClick={() => setSelectedPrice(o.price)}
              style={{
                textAlign: "center", cursor: "pointer",
                background: isSelected ? "rgba(59,130,246,0.08)" : "transparent",
                borderRadius: 4, padding: "2px 0",
              }}
            >
              <div style={{ fontWeight: 600, fontSize: 12, color: priceColor(o.price) }}>
                {fmt(o.price)}
              </div>
              <div style={{ fontSize: 10, color: "#475569", marginTop: 1 }}>
                {pct(o.price)}
              </div>
            </div>

            {/* 잔량 + 바 (왼쪽 정렬) */}
            <div style={{
              position: "relative", textAlign: "left",
              overflow: "hidden", height: "100%",
              display: "flex", alignItems: "center",
            }}>
              <div style={{
                position: "absolute", top: 0, left: 0, bottom: 0,
                width: `${barPct}%`,
                background: "rgba(59,130,246,0.13)",
              }} />
              <span style={{ position: "relative", color: "#94a3b8", fontSize: 11 }}>
                {fmt(o.qty)}
              </span>
            </div>
          </div>
        );
      })}

      {/* ── 하단: 판매대기 / 구매대기 ── */}
      <div style={{
        marginTop: "auto", // 🔥 핵심
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        borderTop: "1px solid #1e2535",
        background: "#0e1117",
      }}>
        <div style={{
          padding: "7px 10px",
          borderRight: "1px solid #1e2535",
        }}>
          <div style={{ fontSize: 10, color: "#475569", marginBottom: 2 }}>판매대기</div>
          <div style={{ fontSize: 12, fontWeight: 700, color: "#3b82f6" }}>
            {fmt(sellOrders.reduce((s, o) => s + o.qty, 0))}
          </div>
        </div>
        <div style={{ padding: "7px 10px", textAlign: "right" }}>
          <div style={{ fontSize: 10, color: "#475569", marginBottom: 2 }}>구매대기</div>
          <div style={{ fontSize: 12, fontWeight: 700, color: "#ef5350" }}>
            {fmt(buyOrders.reduce((s, o) => s + o.qty, 0))}
          </div>
        </div>
      </div>
    </div>
  );
}