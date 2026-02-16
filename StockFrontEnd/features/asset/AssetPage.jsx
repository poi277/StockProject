export default function AssetPage({myasset}) {
    const { totalAsset, holdings } = myasset;
    
    const totalStockValue = holdings.reduce((sum, stock) => {
        return sum + (stock.quantity * stock.averagePrice);
    }, 0);
    
    const totalValue = totalAsset + totalStockValue;
    
    return (
        <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
            {/* 자산 요약 */}
            <div style={{ 
                background: 'white', 
                borderRadius: '8px', 
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                padding: '24px',
                marginBottom: '24px'
            }}>
                <h1 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '16px' }}>
                    내 자산
                </h1>
                
                <div style={{ 
                    display: 'grid', 
                    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                    gap: '16px'
                }}>
                    <div style={{ background: '#eff6ff', padding: '16px', borderRadius: '8px' }}>
                        <p style={{ color: '#6b7280', fontSize: '14px' }}>보유 현금</p>
                        <p style={{ fontSize: '24px', fontWeight: 'bold', color: '#2563eb' }}>
                            {totalAsset.toLocaleString()}원
                        </p>
                    </div>
                    
                    <div style={{ background: '#f0fdf4', padding: '16px', borderRadius: '8px' }}>
                        <p style={{ color: '#6b7280', fontSize: '14px' }}>주식 평가액</p>
                        <p style={{ fontSize: '24px', fontWeight: 'bold', color: '#16a34a' }}>
                            {totalStockValue.toLocaleString()}원
                        </p>
                    </div>
                    
                    <div style={{ background: '#faf5ff', padding: '16px', borderRadius: '8px' }}>
                        <p style={{ color: '#6b7280', fontSize: '14px' }}>총 자산</p>
                        <p style={{ fontSize: '24px', fontWeight: 'bold', color: '#9333ea' }}>
                            {totalValue.toLocaleString()}원
                        </p>
                    </div>
                </div>
            </div>
            
            {/* 보유 주식 */}
            <div style={{ 
                background: 'white', 
                borderRadius: '8px', 
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                padding: '24px'
            }}>
                <h2 style={{ fontSize: '20px', fontWeight: 'bold', marginBottom: '16px' }}>
                    보유 주식
                </h2>
                
                {holdings.length === 0 ? (
                    <p style={{ textAlign: 'center', color: '#6b7280', padding: '32px 0' }}>
                        보유 중인 주식이 없습니다
                    </p>
                ) : (
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr style={{ background: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
                                <th style={{ padding: '12px', textAlign: 'left', fontWeight: '600' }}>종목코드</th>
                                <th style={{ padding: '12px', textAlign: 'right', fontWeight: '600' }}>보유 수량</th>
                                <th style={{ padding: '12px', textAlign: 'right', fontWeight: '600' }}>평균 매수가</th>
                                <th style={{ padding: '12px', textAlign: 'right', fontWeight: '600' }}>평가액</th>
                            </tr>
                        </thead>
                        <tbody>
                            {holdings.map((stock, index) => {
                                const stockValue = stock.quantity * stock.averagePrice;
                                return (
                                    <tr key={index} style={{ borderBottom: '1px solid #e5e7eb' }}>
                                        <td style={{ padding: '12px', fontWeight: '500' }}>{stock.stockCode}</td>
                                        <td style={{ padding: '12px', textAlign: 'right' }}>{stock.quantity.toLocaleString()}주</td>
                                        <td style={{ padding: '12px', textAlign: 'right' }}>{stock.averagePrice.toLocaleString()}원</td>
                                        <td style={{ padding: '12px', textAlign: 'right', fontWeight: '600' }}>{stockValue.toLocaleString()}원</td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
}