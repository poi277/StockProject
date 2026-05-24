export default function useStockHeaderGrid(stock) {
    const low = stock?.lowPrice ?? 0;
    const high = stock?.highPrice ?? 0;
    const close = stock?.closePrice ?? 0;

    // 1일 범위: closePrice가 low~high 사이 몇 %인지
    const range = high - low;
    const leftPercent = range > 0 ? (((close - low) / range) * 100).toFixed(4) : 50;
    const rightPercent = (100 - leftPercent).toFixed(4);

    return {
        low,
        high,
        close,
        leftPercent,
        rightPercent,
    };
}