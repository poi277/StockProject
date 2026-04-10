import './StockRollingBar.css'

export default function StockRollingBar() {
  return (
  <div  className="e9yr874"  data-section-name="지수Rolling" style={{ '--section-width': '1460px'}}>
      <span 
        className="tw3v-1r5dc8g0" 
        style={{
          '--tds-wts-font-weight': 'var(--tw-font-weight-regular)',
          '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)',
          '--tds-wts-line-height': 1.45,
          '--tds-wts-font-size': '14px'
        }}
      >
        <a
      className="tw3v-s67tfwa tw3v-s67tfwc tw3v-s67tfw6 tw3v-s67tfw9"
      data-tossinvest-log="TextButton"
      data-contents-label="투자 유의사항"
      data-contents-label-code="투자 유의사항"  
      data-contents-value="투자 유의사항"
      data-content-tag="투자_유의사항"
      data-parent-name="FooterIndexIndicators"
      href="/investment-disclaimers?menu=stocks"
      target="_blank"
      rel="noreferrer"
    >
      <span className="tw3v-s67tfwb">투자 유의사항</span>
    </a>
      </span>
      <div className="e9yr877"></div>
      <div className="e9yr876">
        <div className="e9yr871" style={{ '--container-width': '2400px', '--animation-duration': '60s' }}>
          {[...Array(20)].map((_, i) => (
            //여기 key는 array뺄때 빼야함
            <span key={i} className="e9yr872">
              <a data-tossinvest-log="Link" data-contents-label="달러 인덱스" data-contents-label-code="name" data-contents-value="달러 인덱스" data-content-tag="name" tabIndex="-1"  className="e9yr873"  href="/indices/RGI..DXY" >
                <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-grey700)', '--tds-wts-line-height': 1.45, '--tds-wts-font-size': '14px',}}>
                  준찬이 엉덩이 때리기
                </span>
                <span className="tw3v-1r5dc8g0 e9yr878" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-grey800)', '--tds-wts-line-height': 1.45, '--tds-wts-font-size': '14px',}}>
                  찰싹
                </span>
                <span className="tw3v-1r5dc8g0 e9yr879" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-red500)', '--tds-wts-line-height': 1.45, '--tds-wts-font-size': '14px', }} >
                  +1 (1%)
                </span>
              </a>
            </span>
            ))}
      </div>
      </div>
      <div className="e9yr875 _150j7tw0"></div>
    </div>
  );
}