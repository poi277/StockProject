'use client'

import { useRouter } from 'next/navigation';
import useWatchList from "./Watchlist";

export default function WatchListForm({ initialWatchList }) {
    const { watchList, handleClick } = useWatchList(initialWatchList);
    const router = useRouter();

    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
            <h1>⭐ 관심 종목</h1>

            <div style={{ marginTop: '20px' }}>
                {watchList?.map((item) => (
                    <div
                        key={item.stockCode}
                        style={{
                            border: '1px solid #ddd',
                            padding: '20px',
                            marginBottom: '10px',
                            borderRadius: '8px',
                            backgroundColor: '#fff',
                            cursor: 'pointer',
                            transition: 'box-shadow 0.2s'
                        }}
                        onClick={() => handleClick(item.stockCode)}
                        onMouseEnter={(e) =>
                            e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)'
                        }
                        onMouseLeave={(e) =>
                            e.currentTarget.style.boxShadow = 'none'
                        }
                    >
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <div>
                                <h3>{item.stockName}</h3>
                                <p style={{ color: '#666' }}>{item.stockCode}</p>
                            </div>

                            <div style={{ textAlign: 'right' }}>
                                <div style={{ fontSize: '28px', fontWeight: 'bold' }}>
                                    {item.closePrice?.toLocaleString() || '0'}원
                                </div>

                                <div style={{
                                    color: (item.changeAmount || 0) >= 0 ? '#d00' : '#00d',
                                    fontWeight: 'bold'
                                }}>
                                    {(item.changeAmount || 0) >= 0 ? '▲' : '▼'}
                                    {Math.abs(item.changeAmount || 0).toLocaleString()}원
                                    ({(item.changeRate || 0).toFixed(2)}%)
                                </div>

                                <div style={{ fontSize: '12px', color: '#999', marginTop: '5px' }}>
                                    시: {item.openPrice?.toLocaleString() || '0'} | 
                                    고: {item.highPrice?.toLocaleString() || '0'} | 
                                    저: {item.lowPrice?.toLocaleString() || '0'}
                                </div>

                                <div style={{ fontSize: '14px', marginTop: '5px' }}>
                                    거래량: {(item.volume || 0).toLocaleString()}주
                                </div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}