import { useEffect, useMemo, useRef, useState } from "react";
import { getOrderbookApi } from "../../../../lib/trade";
import { useOrderWebSocket } from "../../../../util/websocket/context/OrderWebSocketContext";
import { useHogaSocket } from "../../../../util/websocket/useHogaSocket";
import { useExecutionSocket } from "../../../../util/websocket/useExecutionSocket";
import { useStockWebSocket } from "../../../../util/websocket/context/StockWebSocketContext";

const MAX_VISIBLE_ORDERS = 10;

export function getHogaPriceColor(price, yesterdayClosePrice) {
  if (!yesterdayClosePrice || price === yesterdayClosePrice) return "var(--wts-adaptive-grey700)";
  return price > yesterdayClosePrice ? "var(--wts-adaptive-red500)" : "var(--wts-adaptive-blue500)";
}

export function getHogaChangeRateStr(price, yesterdayClosePrice) {
  if (!yesterdayClosePrice) return "0.00%";
  const rate = (price - yesterdayClosePrice) / yesterdayClosePrice * 100;
  return `${rate > 0 ? '+' : ''}${rate.toFixed(2)}%`;
}


// useHoga.js — 상태 여기서만 관리
export default function useHoga(stockCode) {
  const { client, connected } = useOrderWebSocket();
  const { stockConnected, stockClient } = useStockWebSocket();
  const { executions } = useExecutionSocket(stockClient, stockConnected, stockCode);
  const [sellOrders, setSellOrders] = useState([]);
  const [buyOrders, setBuyOrders] = useState([]);
  const scrollRef = useRef(null);

  useEffect(() => {
      if (!scrollRef.current) return;
      const el = scrollRef.current;
      el.scrollTop = (el.scrollHeight - el.clientHeight) / 2;
  }, []);

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

  // 매도는 내림차순으로 정렬돼있으니 뒤에서 10개 (현재가에 가까운 낮은 가격 쪽)
  const visibleSellOrders = useMemo(
    () => sellOrders.slice(-MAX_VISIBLE_ORDERS),
    [sellOrders]
  );

  // 매수는 내림차순으로 정렬돼있으니 앞에서 10개 (현재가에 가까운 높은 가격 쪽)
  const visibleBuyOrders = useMemo(
    () => buyOrders.slice(0, MAX_VISIBLE_ORDERS),
    [buyOrders]
  );

  const totalSellQuantity = useMemo(() => sellOrders.reduce((s, o) => s + o.quantity, 0), [sellOrders]);
  const totalBuyQuantity  = useMemo(() => buyOrders.reduce((s, o) => s + o.quantity, 0), [buyOrders]);
  const getBarWidth = (quantity) => `calc(${(quantity / maxQuantity) * 100}% - 8px)`;

  const lastExecutionPrice = useMemo(() => 
    executions[0]?.price ?? null
    , [executions]);

  return {
    scrollRef,
    sellOrders: visibleSellOrders,
    buyOrders: visibleBuyOrders,
    maxQuantity,
    getBarWidth,
    totalSellQuantity,
    totalBuyQuantity,
    executions,
    lastExecutionPrice,
    getHogaPriceColor,
    getHogaChangeRateStr
  };
}