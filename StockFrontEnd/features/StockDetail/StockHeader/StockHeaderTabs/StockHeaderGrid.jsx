import React from 'react';

export default function StockHeaderGrid() {
  return (
    <div className="w65d0">
      <div className="w65d6">
        {['1일 범위', '52주 범위'].map((label) => (
          <span key={label} className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity500)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>{label}</span>
        ))}

        {['199,800원', '52,000원'].map((val) => (
          <span key={val} className="tw3v-1r5dc8g0 w65d7" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}><span>{val}</span></span>
        ))}

        {[{ left: '67.6871', right: '32.3129' }, { left: '89.3768', right: '10.6232' }].map((bar, i) => (
          <div key={i} className="w65d3"><div className="w65d4" style={{ flex: `${bar.left} 1 0%` }}></div><div className="w65d5"></div><div className="w65d4" style={{ flex: `${bar.right} 1 0%` }}></div></div>
        ))}

        {['214,500원', '228,500원'].map((val) => (
          <span key={val} className="tw3v-1r5dc8g0 w65d7" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}><span>{val}</span></span>
        ))}
      </div>

      {[
        [{ label: '거래대금', value: '1위', arrow: '▼' }, { label: '체결강도', value: '124.58%' }],
        [{ label: '외국인 순매수', value: '2위', arrow: '-' }, { label: '외국인 순매도', value: '100위 밖' }],
        [{ label: '기관 순매수', value: '1위', arrow: '-' }, { label: '기관 순매도', value: '100위 밖' }],
        [{ label: '시가총액 순위', value: '1위' }, { label: '시가총액', value: '1,247.52조원' }],
      ].map((section, i) => (
        <div key={i} className="w65d1">
          {section.map(({ label, value, arrow }) => (
            <div key={label} className="w65dc w65db" data-state="closed" style={{ display: 'grid' }}>
              <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity500)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>{label}</span>
              <span className="tw3v-1r5dc8g0 w65d2" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px', display: 'flex' }}>
                <span>{value}</span>{arrow && <span className="w65da">{arrow}</span>}
              </span>
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}