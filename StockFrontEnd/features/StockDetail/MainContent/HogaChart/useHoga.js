import { useEffect, useMemo, useState } from "react";
import { getOrderbookApi } from "../../../../lib/trade";
import { useWebSocket } from "../../../../util/WebSocketContext";
import { useHogaSocket } from "../../../../util/useHogaSocket";
import { useExecutionSocket } from "../../../../util/useExecutionSocket";

// useHoga.js — 상태 여기서만 관리
export default function useHoga(stockCode) {
  const { client, connected } = useWebSocket();
  const { executions } = useExecutionSocket(client, connected, stockCode);
  const [sellOrders, setSellOrders] = useState([]);
  const [buyOrders, setBuyOrders] = useState([]);

  // 1. 초기 데이터 REST fetch
  useEffect(() => {
    if (!stockCode) return;
    getOrderbookApi(stockCode).then(res => {
    const data = res.data ?? {}
    setSellOrders((data.sellOrders ?? [])
    .map(o => ({ price: o.tradePrice, quantity: o.remainingQuantity }))
    .sort((a, b) => b.price - a.price) // 내림차순
    );
    setBuyOrders((data.buyOrders ?? [])
    .map(o => ({ price: o.tradePrice, quantity: o.remainingQuantity }))
    .sort((a, b) => b.price - a.price) // 내림차순
    );
    });
  }, [stockCode]);

  // 2. 웹소켓 업데이트 — 콜백으로 상태 업데이트
  useHogaSocket(client, connected, stockCode, {
    onSellUpdate: ({ price, qty }) => {
        setSellOrders(prev => {
        if (qty === 0) return prev.filter(o => o.price !== price);
        const exists = prev.some(o => o.price === price);
        if (exists) return prev.map(o => o.price === price ? { ...o, quantity: qty } : o);
        return [...prev, { price, quantity: qty }].sort((a, b) => b.price - a.price); // 내림차순
    });
    },
    onBuyUpdate: ({ price, qty }) => {
     setBuyOrders(prev => {
        if (qty === 0) return prev.filter(o => o.price !== price);
        const exists = prev.some(o => o.price === price);
        if (exists) return prev.map(o => o.price === price ? { ...o, quantity: qty } : o);
        return [...prev, { price, quantity: qty }].sort((a, b) => b.price - a.price); // 내림차순 유지
    });
    },
  });

  // 3. 파생값 계산
  const maxQuantity = useMemo(() => {
    const all = [...sellOrders, ...buyOrders];
    return all.length === 0 ? 1 : Math.max(...all.map(o => o.quantity));
  }, [sellOrders, buyOrders]);

  const totalSellQuantity = useMemo(() => sellOrders.reduce((s, o) => s + o.quantity, 0), [sellOrders]);
  const totalBuyQuantity  = useMemo(() => buyOrders.reduce((s, o) => s + o.quantity, 0), [buyOrders]);
  const getBarWidth = (quantity) => `calc(${(quantity / maxQuantity) * 100}% - 8px)`;

  const lastExecutionPrice = useMemo(() => 
    executions[0]?.price ?? null
    , [executions]);

  return { sellOrders, buyOrders, maxQuantity, getBarWidth, totalSellQuantity, totalBuyQuantity,executions,lastExecutionPrice };
}