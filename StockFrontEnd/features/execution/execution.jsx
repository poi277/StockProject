'use client';

import { useExecutionSocket } from "../../util/useExecutionSocket";
import { useWebSocket } from "../../util/WebSocket";
import styles from '../../css/ExecutionList.module.css';

export default function ExecutionList({ stockCode }) {
    const { connected, client } = useWebSocket();
    const { executions } = useExecutionSocket(client, connected, stockCode);

    const fmt = (n) => Number(n).toLocaleString('ko-KR');

    return (
        <div className={styles.wrap}>
            <div className={styles.header}>
                <span>체결가</span>
                <span>체결량</span>
            </div>
            <div className={styles.list}>
                {executions.map((ex, index) => (
                    <div key={index} className={styles.row}>
                        <span
                            className={styles.price}
                            style={{ color: ex.tradeType === 'BUY' ? '#ff3b30' : '#0056e0' }}
                        >
                            {fmt(ex.price)}
                        </span>
                        <span
                            className={styles.qty}
                            style={{ color: ex.tradeType === 'BUY' ? '#ff3b30' : '#0056e0' }}
                        >
                            {fmt(ex.quantity)}
                        </span>
                    </div>
                ))}
            </div>
        </div>
    );
}