import React from 'react';

export default function StockHeaderPrice({ stock }) {
  return (
    <div className="_1sivumi0 _1sivumi2">
      <div style={{ display: 'flex', flexDirection: 'row', gap: '0px', alignItems: 'center' }}>
        <span className="tw3v-1r5dc8g0 _1lqnwjh1" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-bold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-grey900)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '20px' }}><span className="_1p5yqoh0">{stock.currentPrice.toLocaleString()}원</span></span>
        <span className="tw3v-1r5dc8g0 _1p5yqoh0" style={{ marginLeft: '4px', '--tds-wts-font-weight': 'var(--tw-font-weight-bold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '14px' }}><span className="_1p5yqoh0">$0.01</span></span>
        <span className="tw3v-1r5dc8g0 _1sivumi5" style={{ display: 'flex', '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity600)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '14px' }}></span>
        <div className="_1p5yqoh0 _1sivumia" style={{ display: 'flex', alignItems: 'center', marginLeft: '2px' }}>
          <span className="tw3v-1r5dc8g0 _1sivumia" style={{ marginRight: '6px', '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-grey700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '14px' }}>어제보다</span>
          <span
            className="tw3v-1r5dc8g0"
            style={{
              '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)',
              '--tds-wts-foreground-color': stock.changeAmount === 0
                ? 'var(--wts-adaptive-grey600)'
                : stock.changeAmount < 0
                  ? 'var(--wts-adaptive-blue500)'
                  : 'var(--wts-adaptive-red500)',
              '--tds-wts-line-height': '1.45',
              '--tds-wts-font-size': '14px'
            }}
          >
            {stock.changeAmount.toLocaleString()}원({stock.changeRate}%)
          </span>
        </div>
        <div style={{ flex: '0 0 auto', width: '6px' }}></div>
        <span className="tw3v-1r5dc8g0 vda5510" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity600)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '14px' }}>애프터마켓에서 <span className="_1p5yqoh0" style={{ color: 'var(--wts-adaptive-blue500)', marginLeft: '4px' }}>-999원 (99.99%)</span></span>
      </div>
    </div>
  );
}