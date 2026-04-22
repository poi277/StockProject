import React from 'react';

/**
 * 이미지 [image_7b3c43.png]와 [image_8.png]의 대조 결과를 바탕으로
 * 누락된 '_1sivumi5' 계층을 정확한 위치에 포함하여 완벽하게 재현한 컴포넌트
 */
export default function StockHeaderPrice({stock}) {
  return (
    <div className="_1sivumi0 _1sivumi2">
      <div style={{ display: 'flex', flexDirection: 'row', gap: '0px', alignItems: 'center' }}>
        
        {/* 1. 현재 주가 */}
        <span 
          className="tw3v-1r5dc8g0 _1lqnwjh1" 
          style={{ 
            '--tds-wts-font-weight': 'var(--tw-font-weight-bold)', 
            '--tds-wts-foreground-color': 'var(--wts-adaptive-grey900)', 
            '--tds-wts-line-height': '1.45', 
            '--tds-wts-font-size': '20px' 
          }}
        >
          <span className="_1p5yqoh0">{stock.closePrice.toLocaleString()}원</span>
        </span>

        {/* 2. 달러 환산가 */}
        <span 
          className="tw3v-1r5dc8g0 _1p5yqoh0" 
          style={{ 
            marginLeft: '4px',
            '--tds-wts-font-weight': 'var(--tw-font-weight-bold)', 
            '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', 
            '--tds-wts-line-height': '1.45', 
            '--tds-wts-font-size': '14px' 
          }}
        >
          <span className="_1p5yqoh0">$0.01</span>
        </span>
        <span 
          className="tw3v-1r5dc8g0 _1sivumi5" 
          style={{ 
            display: 'flex', 
            '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', 
            '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity600)', 
            '--tds-wts-line-height': '1.45', 
            '--tds-wts-font-size': '14px' 
          }}
        >
          {/* 이미지 속에는 비어있지만, 레이아웃 계산을 위한 미세 간격 역할을 합니다 */}
        </span>

        {/* 3. 장 마감 정보 및 등락률 컨테이너 (div class="_1p5yqoh0 _1sivumia") */}
        <div className="_1p5yqoh0 _1sivumia" style={{ display: 'flex', alignItems: 'center', marginLeft: '2px' }}>
          <span 
            className="tw3v-1r5dc8g0 _1sivumia" 
            style={{ 
              marginRight: '6px', 
              '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', 
              '--tds-wts-foreground-color': 'var(--wts-adaptive-grey700)', 
              '--tds-wts-line-height': '1.45', 
              '--tds-wts-font-size': '14px' 
            }}
          >
            어제보다
          </span>
          <span className="tw3v-1r5dc8g0" style={{
              '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)',
              '--tds-wts-foreground-color': stock.changeAmount < 0  ? 'var(--wts-adaptive-blue500)' : 'var(--wts-adaptive-red500)', '--tds-wts-line-height': '1.45',
              '--tds-wts-font-size': '14px'
            }}
          >
            {stock.changeAmount.toLocaleString()}원({stock.changeRate}%)
          </span>
        </div>

        {/* 이하 애프터마켓 정보 구조 유지 */}
        <div style={{ flex: '0 0 auto', width: '6px' }}></div>
        <span className="tw3v-1r5dc8g0 vda5510" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity600)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '14px' }}>
          애프터마켓에서 <span className="_1p5yqoh0" style={{ color: 'var(--wts-adaptive-blue500)', marginLeft: '4px' }}>-999원 (99.99%)</span>
        </span>
         </div>
      </div>
  );
}