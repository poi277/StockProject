'use client';

import { useState, useEffect } from 'react';
import styles from '../../css/HogaChart.module.css';
import { getOrdersApi } from '../../lib/trade';
import { useHogaSocket } from '../../util/useHogaSocket';
import { useWebSocket } from '../../util/WebSocket';

function getUnit(price) {
    return price >= 500000 ? 1000
         : price >= 100000 ? 500
         : price >= 50000  ? 100
         : price >= 10000  ? 50
         : 10;
}

function buildLevels(center) {
    const unit = getUnit(center);
    const sellPrices = [4, 3, 2, 1, 0].map(i => center + unit * i); 
    const buyPrices  = [1, 2, 3, 4].map(i => center - unit * i);
    return { sellPrices, buyPrices };
}

export default function HogaChart({ currentStock, selectedPrice, setSelectedPrice }) {
    const { connected, client } = useWebSocket();
    const { hogas } = useHogaSocket(client, connected, currentStock?.stockCode);
    const [sellOrders, setSellOrders] = useState([]);
    const [buyOrders, setBuyOrders]   = useState([]);

    const hoga       = hogas[currentStock?.stockCode];
    const closePrice = hoga?.currentPrice || currentStock?.closePrice || 0;
    const changeAmt  = currentStock?.changeAmount || 0;
    const changeRate = currentStock?.changeRate   || 0;
    const isUp       = changeAmt >= 0;
    const openPrice  = currentStock?.openPrice || closePrice || 0; // ✅ 시가 기준

    // 가격 색상 - 전날 종가 기준
    const priceColor = (price) => {
    if (!openPrice) return '#333';
    return price > openPrice ? '#ff3b30' : price < openPrice ? '#0056e0' : '#333';
};


    // 초기 렌더링
    useEffect(() => {
        if (!closePrice || !currentStock?.stockCode) return;
        const { sellPrices, buyPrices } = buildLevels(closePrice);

        getOrdersApi(currentStock.stockCode)
            .then(res => {
                const sellDb = res.data?.sellOrders || [];
                const buyDb  = res.data?.buyOrders  || [];

                setSellOrders(sellPrices.map(price => ({
                    price,
                    qty: sellDb.filter(o => o.tradePrice === price)
                               .reduce((s, o) => s + o.remainingQuantity, 0)
                })));
                setBuyOrders(buyPrices.map(price => ({
                    price,
                    qty: buyDb.filter(o => o.tradePrice === price)
                              .reduce((s, o) => s + o.remainingQuantity, 0)
                })));
            })
            .catch(() => {
                setSellOrders(sellPrices.map(price => ({ price, qty: 0 })));
                setBuyOrders(buyPrices.map(price => ({ price, qty: 0 })));
            });
    }, [currentStock?.stockCode]);

    // 웹소켓 수신 - 현재가 기준으로 리스트 재구성
    useEffect(() => {
        if (!hoga) return;
        const newCenter = hoga.currentPrice || currentStock?.closePrice;
        if (!newCenter) return;

        const { sellPrices, buyPrices } = buildLevels(newCenter);

        setSellOrders(sellPrices.map(price => {
            const found = hoga.sellOrders?.find(o => o.tradePrice === price);
            return { price, qty: found ? found.remainingQuantity : 0 };
        }));
        setBuyOrders(buyPrices.map(price => {
            const found = hoga.buyOrders?.find(o => o.tradePrice === price);
            return { price, qty: found ? found.remainingQuantity : 0 };
        }));
    }, [hogas, currentStock?.stockCode]);

    const pct = (price) => {
        if (!openPrice) return '0.00%';
        const v = ((price - openPrice) / openPrice * 100).toFixed(2);
        return (v > 0 ? '+' : '') + v + '%';
    };

    const fmt    = (n) => Number(n).toLocaleString('ko-KR');
    const allQtys = [...sellOrders.map(o => o.qty), ...buyOrders.map(o => o.qty)];
    const maxQty  = Math.max(...allQtys, 1);

    return (
        <div className={styles.hogaWrap}>
            <div className={styles.hogaHeader}>
                <span className={styles.currentPrice} style={{ color: isUp ? '#ff3b30' : '#0056e0' }}>
                    {fmt(closePrice)}
                </span>
                <span className={styles.changeText} style={{ color: isUp ? '#ff3b30' : '#0056e0' }}>
                    {isUp ? '▲' : '▼'} {fmt(Math.abs(changeAmt))} ({Math.abs(changeRate).toFixed(2)}%)
                </span>
            </div>

            <div className={styles.hogaBody}>
                <div className={styles.hogaTable}>

                    {/* 매도 호가 */}
                    {sellOrders.map(o => {
                        const barW = Math.round((o.qty / maxQty) * 70);
                        return (
                            <div key={o.price} className={styles.hogaRow}>
                                <div className={`${styles.qtyCell} ${styles.right}`}>
                                    <div className={styles.sellBar} style={{ width: barW }} />
                                    <span>{o.qty === 0 ? '-' : fmt(o.qty)}</span>
                                </div>
                                <div
                                    className={`${styles.priceCell} ${selectedPrice === o.price ? styles.selected : ''}`}
                                    style={{ color: priceColor(o.price) }} // ✅ 전날 종가 기준 색상
                                    onClick={() => setSelectedPrice(o.price)}
                                >
                                    {fmt(o.price)}
                                </div>
                                <div className={styles.pctCell} style={{ color: priceColor(o.price) }}>
                                    {pct(o.price)}
                                </div>
                            </div>
                        );
                    })}

                    {/* 매수 호가 */}
                    {buyOrders.map(o => {
                        const barW = Math.round((o.qty / maxQty) * 70);
                        return (
                            <div key={o.price} className={styles.hogaRow}>
                                <div className={styles.pctCell} style={{ color: priceColor(o.price) }}>
                                    {pct(o.price)}
                                </div>
                                <div
                                    className={`${styles.priceCell} ${selectedPrice === o.price ? styles.selected : ''}`}
                                    style={{ color: priceColor(o.price) }} // ✅ 전날 종가 기준 색상
                                    onClick={() => setSelectedPrice(o.price)}
                                >
                                    {fmt(o.price)}
                                </div>
                                <div className={`${styles.qtyCell} ${styles.left}`}>
                                    <div className={styles.buyBar} style={{ width: barW }} />
                                    <span>{o.qty === 0 ? '-' : fmt(o.qty)}</span>
                                </div>
                            </div>
                        );
                    })}
                </div>

                <div className={styles.hogaInfo}>
                    <Info label="시가" value={currentStock?.openPrice} />
                    <Info label="고가" value={currentStock?.highPrice} red />
                    <Info label="저가" value={currentStock?.lowPrice} />
                    <Info label="거래량" value={currentStock?.volume} unit="주" />
                </div>
            </div>
        </div>
    );
}

function Info({ label, value, red, unit = "원" }) {
    const fmt = (n) => n ? Number(n).toLocaleString() : '-';
    return (
        <div className={styles.infoRow}>
            <span className={styles.infoLabel}>{label}</span>
            <span className={styles.infoVal} style={{ color: red ? '#ff3b30' : '#0056e0' }}>
                {fmt(value)}{unit}
            </span>
        </div>
    );
}