'use client';

import { useExecutionSocket } from "../../util/useExecutionSocket";
import { useWebSocket } from "../../util/WebSocket";
import styles from '../../css/HogaExecution.module.css';

export default function hogaExecution({ stockCode }) {
    const { connected, client } = useWebSocket();
    const { executions } = useExecutionSocket(client, connected, stockCode);

    const fmt = (n) => Number(n).toLocaleString('ko-KR');
    const fmtRate = (n) => (n >= 0 ? '+' : '') + Number(n).toFixed(2) + '%';
    const fmtTime = (t) => t ? t.substring(11, 19) : ''; // HH:mm:ss

    return (
        <div className={styles.wrap}>
            <div className={styles.header}>
                <span>체결가</span>
                <span>체결량 (주)</span>
            </div>
            <div className={styles.list}>
                {executions.map((ex, index) => {
                    const isUp = ex.changeRate >= 0;
                    return (
                        <div key={index} className={styles.row}>
                            <span style={{ color: isUp ? '#ff3b30' : '#0056e0' }}>
                                {fmt(ex.price)}원
                            </span>
                            <span style={{ color: isUp ? '#ff3b30' : '#0056e0' }}>
                                {fmt(ex.quantity)}
                            </span>
                            <span style={{ color: isUp ? '#ff3b30' : '#0056e0' }}>
                                {fmtRate(ex.changeRate)}
                            </span>
                            <span>{fmt(ex.totalVolume)}</span>
                            <span>{fmtTime(ex.time)}</span>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}