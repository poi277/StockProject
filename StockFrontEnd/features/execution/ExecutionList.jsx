'use client';

import { useExecutionSocket } from "../../util/useExecutionSocket";
import { useWebSocket } from "../../util/WebSocket";
import styles from '../../css/ExecutionList.module.css';

export default function ExecutionList({ stockCode }) {
    const { connected, client } = useWebSocket();
    const { executions } = useExecutionSocket(client, connected, stockCode);

    const fmt = (n) => Number(n).toLocaleString('ko-KR');
    const fmtRate = (n) => (n >= 0 ? '+' : '') + Number(n).toFixed(2) + '%';
    const fmtTime = (t) => t ? t.substring(11, 19) : '';

    return (
        <div className={styles.wrap}>
            <div className={styles.header}>
                <span>체결가</span>
                <span>체결량 (주)</span>
                <span>등락률</span>
                <span>거래량 (주)</span>
                <span>시간</span>
            </div>
            <div className={styles.list}>
                {executions.map((ex, index) => {
                    const isBuy = ex.tradeType === 'BUY';
                    const qtyColor = isBuy ? '#ff3b30' : '#0056e0';
                    const rateColor = ex.changeRate >= 0 ? '#ff3b30' : '#0056e0';
                    return (
                        <div key={index} className={styles.row}>
                            <span>{fmt(ex.price)}원</span>
                            <span style={{ color: qtyColor }}>{fmt(ex.quantity)}</span>
                            <span style={{ color: rateColor }}>{fmtRate(ex.changeRate)}</span>
                            <span>{fmt(ex.totalVolume)}</span>
                            <span>{fmtTime(ex.time)}</span>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}